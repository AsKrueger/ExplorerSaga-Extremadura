"""Descarga de las fuentes abiertas con caché en disco.

Cada fuente se guarda tal cual en ``.cache/`` para que la generación sea
reproducible: con la misma caché, ``build`` produce exactamente el mismo
paquete. ``fetch`` solo vuelve a descargar con ``--refresh``.
"""

from __future__ import annotations

import csv
import io
import json
import os
import time
import urllib.error
import urllib.parse
import urllib.request
from pathlib import Path

USER_AGENT = os.environ.get(
    "DATAFACTORY_USER_AGENT", "open-tourism-datafactory/1.0 (python-urllib)"
)
OVERPASS_URL = os.environ.get("OVERPASS_URL", "https://overpass-api.de/api/interpreter")
WIKIDATA_SPARQL = "https://query.wikidata.org/sparql"
COMMONS_API = "https://commons.wikimedia.org/w/api.php"

# Relación de OSM de la comunidad autónoma de Extremadura
EXTREMADURA_AREA = 3600000000 + 349050

JUNTA_DATASETS = {
    "hoteles": "https://www.juntaex.es/documents/77055/5801338/AlojamientosHoteleros.csv",
}

# Filtros de Overpass: lo que interesa a un viajero, siempre con nombre
POI_FILTERS = """
  nwr(area.a)[tourism~"^(museum|gallery|attraction|viewpoint|hotel|hostel|guest_house|apartment|motel|camp_site|caravan_site|chalet|alpine_hut)$"][name];
  nwr(area.a)[tourism=information][information=office][name];
  nwr(area.a)[historic~"^(castle|monument|archaeological_site|ruins|city_gate|fort|tower|manor|palace|aqueduct|bridge|monastery|church|citywalls)$"][name];
  nwr(area.a)[historic][wikidata][name];
  nwr(area.a)[heritage][name];
  nwr(area.a)[amenity=place_of_worship][wikidata][name];
  nwr(area.a)[leisure~"^(bathing_place|swimming_area|nature_reserve)$"][name];
  nwr(area.a)[boundary=protected_area][name];
  nwr(area.a)[natural~"^(waterfall|cave_entrance|beach|gorge)$"][name];
  nwr(area.a)[natural=peak][wikidata][name];
  nwr(area.a)[waterway=waterfall][name];
  nwr(area.a)[amenity~"^(restaurant|cafe|bar|pub|ice_cream)$"][name];
  nwr(area.a)[shop~"^(gift|souvenir|deli|farm|wine|cheese|books)$"][name];
  nwr(area.a)[amenity=pharmacy];
  nwr(area.a)[amenity~"^(hospital|clinic)$"][name];
  nwr(area.a)[amenity=charging_station];
"""


def _request(url: str, data: bytes | None = None, headers: dict | None = None,
             timeout: int = 300, retries: int = 4) -> bytes:
    all_headers = {"User-Agent": USER_AGENT, **(headers or {})}
    for attempt in range(retries):
        try:
            req = urllib.request.Request(url, data=data, headers=all_headers)
            with urllib.request.urlopen(req, timeout=timeout) as resp:
                return resp.read()
        except (urllib.error.URLError, TimeoutError) as error:
            if attempt == retries - 1:
                raise
            wait = 15 * (attempt + 1)
            print(f"  reintento en {wait}s ({error})")
            time.sleep(wait)
    raise RuntimeError("unreachable")


def overpass(query: str) -> dict:
    body = urllib.parse.urlencode({"data": query}).encode()
    return json.loads(_request(OVERPASS_URL, data=body, timeout=1200))


def fetch_overpass(cache: Path) -> None:
    head = f"[out:json][timeout:900][maxsize:1073741824];area({EXTREMADURA_AREA})->.a;"
    municipalities_query = head + (
        "rel(area.a)[boundary=administrative][admin_level=8];out body geom qt;")
    print("Overpass: municipios con sus límites…")
    _write_json(cache / "osm_municipalities.json", overpass(municipalities_query))

    print("Overpass: lugares de interés (puede tardar varios minutos)…")
    _write_json(cache / "osm_pois.json", overpass(head + "(" + POI_FILTERS + ");out center tags qt;"))


SIGHTS_QUERY = """
SELECT DISTINCT ?item ?coord WHERE {
  ?item wdt:P131+ wd:Q5777 ; wdt:P625 ?coord .
  { ?item wdt:P1435 [] }
  UNION {
    ?item wdt:P31/wdt:P279* ?root .
    VALUES ?root { wd:Q811979 wd:Q33506 wd:Q839954 wd:Q4989906 wd:Q473972 wd:Q34038 wd:Q35509 }
    ?article schema:about ?item ; schema:isPartOf ?wiki .
    VALUES ?wiki { <https://es.wikipedia.org/> <https://en.wikipedia.org/> }
  }
}"""

SIGHT_LABELS_QUERY = """
SELECT ?item ?labelEs ?labelEn ?classLabel WHERE {
  VALUES ?item { %s }
  OPTIONAL { ?item rdfs:label ?labelEs FILTER(LANG(?labelEs) = "es") }
  OPTIONAL { ?item rdfs:label ?labelEn FILTER(LANG(?labelEn) = "en") }
  OPTIONAL { ?item wdt:P31 ?class . ?class rdfs:label ?classLabel FILTER(LANG(?classLabel) = "en") }
}"""


def _sparql(query: str) -> list[dict]:
    body = urllib.parse.urlencode({"query": query, "format": "json"}).encode()
    result = json.loads(_request(WIKIDATA_SPARQL, data=body,
                                 headers={"Accept": "application/sparql-results+json"}))
    return [{key: value["value"] for key, value in binding.items()}
            for binding in result["results"]["bindings"]]


def fetch_wikidata_sights(cache: Path) -> None:
    """Monumentos protegidos, museos, yacimientos y espacios naturales de
    Extremadura en Wikidata, estén o no en OSM con su etiqueta wikidata."""
    print("Wikidata: lugares de interés de Extremadura…")
    rows = _sparql(SIGHTS_QUERY)
    qids = sorted({row["item"].rsplit("/", 1)[-1] for row in rows})
    for start in range(0, len(qids), 200):
        chunk = " ".join(f"wd:{q}" for q in qids[start:start + 200])
        rows.extend(_sparql(SIGHT_LABELS_QUERY % chunk))
        time.sleep(1)
    print(f"  {len(qids)} lugares")
    _write_json(cache / "wikidata_sights.json", rows)


def wikidata_entities(qids: list[str]) -> list[dict]:
    """Etiquetas, descripciones, imagen y artículos de Wikipedia en es/en."""
    rows: list[dict] = []
    for start in range(0, len(qids), 250):
        chunk = " ".join(f"wd:{q}" for q in qids[start:start + 250])
        query = f"""
SELECT ?item ?labelEs ?labelEn ?descEs ?descEn ?image ?wpEs ?wpEn ?heritage WHERE {{
  VALUES ?item {{ {chunk} }}
  OPTIONAL {{ ?item rdfs:label ?labelEs FILTER(LANG(?labelEs) = "es") }}
  OPTIONAL {{ ?item rdfs:label ?labelEn FILTER(LANG(?labelEn) = "en") }}
  OPTIONAL {{ ?item schema:description ?descEs FILTER(LANG(?descEs) = "es") }}
  OPTIONAL {{ ?item schema:description ?descEn FILTER(LANG(?descEn) = "en") }}
  OPTIONAL {{ ?item wdt:P18 ?image }}
  OPTIONAL {{ ?wpEsUrl schema:about ?item; schema:isPartOf <https://es.wikipedia.org/>; schema:name ?wpEs }}
  OPTIONAL {{ ?wpEnUrl schema:about ?item; schema:isPartOf <https://en.wikipedia.org/>; schema:name ?wpEn }}
  OPTIONAL {{ ?item wdt:P1435 ?heritage }}
}}"""
        body = urllib.parse.urlencode({"query": query, "format": "json"}).encode()
        result = json.loads(_request(WIKIDATA_SPARQL, data=body,
                                     headers={"Accept": "application/sparql-results+json"}))
        for binding in result["results"]["bindings"]:
            rows.append({key: value["value"] for key, value in binding.items()})
        print(f"  Wikidata {min(start + 250, len(qids))}/{len(qids)}")
        time.sleep(1)
    return rows


def wikipedia_extracts(lang: str, titles: list[str]) -> dict[str, str]:
    """Introducción en texto plano de cada artículo (máx. 20 por petición)."""
    extracts: dict[str, str] = {}
    api = f"https://{lang}.wikipedia.org/w/api.php"
    for start in range(0, len(titles), 20):
        chunk = titles[start:start + 20]
        params = urllib.parse.urlencode({
            "action": "query", "format": "json", "formatversion": "2",
            "prop": "extracts", "exintro": "1", "explaintext": "1",
            "exsentences": "4", "redirects": "1", "titles": "|".join(chunk),
        })
        data = json.loads(_request(f"{api}?{params}"))
        query = data.get("query", {})
        redirects = {r["to"]: r["from"] for r in query.get("redirects", [])}
        normalized = {n["to"]: n["from"] for n in query.get("normalized", [])}
        for page in query.get("pages", []):
            title = page.get("title", "")
            original = redirects.get(title, title)
            original = normalized.get(original, original)
            if page.get("extract"):
                extracts[original] = page["extract"]
        time.sleep(1)  # Wikimedia limita las peticiones seguidas (HTTP 429)
        if start % 400 == 0:
            print(f"  Wikipedia {lang} {min(start + 20, len(titles))}/{len(titles)}")
    return extracts


def commons_images(files: list[str]) -> dict[str, dict]:
    """Miniatura, autor y licencia de cada archivo de Commons."""
    info: dict[str, dict] = {}
    for start in range(0, len(files), 40):
        chunk = files[start:start + 40]
        params = urllib.parse.urlencode({
            "action": "query", "format": "json", "formatversion": "2",
            "prop": "imageinfo", "iiprop": "url|extmetadata", "iiurlwidth": "960",
            "iiextmetadatafilter": "Artist|LicenseShortName|LicenseUrl|AttributionRequired",
            "titles": "|".join(f"File:{name}" for name in chunk),
        })
        data = json.loads(_request(f"{COMMONS_API}?{params}"))
        query = data.get("query", {})
        normalized = {n["to"]: n["from"] for n in query.get("normalized", [])}
        for page in query.get("pages", []):
            if "imageinfo" not in page:
                continue
            title = normalized.get(page["title"], page["title"])
            image = page["imageinfo"][0]
            meta = image.get("extmetadata", {})
            info[title.removeprefix("File:")] = {
                "thumb": image.get("thumburl"),
                "page": image.get("descriptionurl"),
                "artist": meta.get("Artist", {}).get("value", ""),
                "license": meta.get("LicenseShortName", {}).get("value", ""),
            }
        time.sleep(1)
        if start % 400 == 0:
            print(f"  Commons {min(start + 40, len(files))}/{len(files)}")
    return info


def fetch_junta(cache: Path) -> None:
    for name, url in JUNTA_DATASETS.items():
        print(f"Junta: {name}…")
        raw = _request(url)
        text = raw.decode("utf-8") if raw.startswith(b"\xef\xbb\xbf") else raw.decode("latin-1")
        rows = list(csv.DictReader(io.StringIO(text.lstrip("﻿"))))
        _write_json(cache / f"junta_{name}.json", rows)


def fetch_enrichment(cache: Path, qids: list[str]) -> None:
    print(f"Wikidata: {len(qids)} entidades…")
    rows = wikidata_entities(qids)
    _write_json(cache / "wikidata.json", rows)

    es_titles = sorted({r["wpEs"] for r in rows if r.get("wpEs")})
    en_titles = sorted({r["wpEn"] for r in rows if r.get("wpEn")})
    print(f"Wikipedia: {len(es_titles)} es, {len(en_titles)} en…")
    _write_json(cache / "wikipedia_es.json", wikipedia_extracts("es", es_titles))
    _write_json(cache / "wikipedia_en.json", wikipedia_extracts("en", en_titles))

    files = sorted({urllib.parse.unquote(r["image"].rsplit("/", 1)[-1]).replace("_", " ")
                    for r in rows if r.get("image")})
    print(f"Commons: {len(files)} imágenes…")
    _write_json(cache / "commons.json", commons_images(files))


def load_json(path: Path):
    with path.open(encoding="utf-8") as handle:
        return json.load(handle)


def _write_json(path: Path, data) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", encoding="utf-8") as handle:
        json.dump(data, handle, ensure_ascii=False, sort_keys=True)
