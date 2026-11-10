"""Transformaciones puras: de las fuentes en caché al paquete de contenido.

No hay red ni reloj aquí, así que la misma entrada produce siempre la misma
salida (generación reproducible e idempotente).
"""

from __future__ import annotations

import html
import math
import re
import unicodedata
import urllib.parse

SCHEMA_VERSION = 1

# categoría -> grupo que ve el usuario en la app
CATEGORY_GROUP = {
    "castle": "HERITAGE", "monument": "HERITAGE", "archaeology": "HERITAGE",
    "religious": "HERITAGE", "palace": "HERITAGE", "bridge": "HERITAGE",
    "museum": "MUSEUMS", "gallery": "MUSEUMS",
    "viewpoint": "NATURE", "protected_area": "NATURE", "waterfall": "NATURE",
    "cave": "NATURE", "peak": "NATURE", "attraction": "NATURE",
    "bathing": "BATHING",
    "restaurant": "FOOD", "cafe": "FOOD", "bar": "FOOD", "ice_cream": "FOOD",
    "hotel": "SLEEP", "hostel": "SLEEP", "rural": "SLEEP", "apartment": "SLEEP",
    "camping": "SLEEP",
    "gift": "SHOPS", "local_products": "SHOPS", "books": "SHOPS",
    "tourist_info": "SERVICES", "pharmacy": "SERVICES", "charging": "SERVICES",
    "health": "SERVICES",
}

_HERITAGE_HISTORIC = {
    "castle": "castle", "fort": "castle", "tower": "castle", "citywalls": "castle",
    "city_gate": "monument", "monument": "monument", "manor": "palace",
    "palace": "palace", "archaeological_site": "archaeology", "ruins": "archaeology",
    "aqueduct": "archaeology", "bridge": "bridge", "church": "religious",
    "monastery": "religious",
}

_FREE_LICENSE = re.compile(r"^(cc[ -]by|cc0|public domain|pd|gfdl|fal|attribution)", re.I)
_TAG = re.compile(r"<[^>]+>")


def normalize(text: str | None) -> str:
    """Minúsculas, sin tildes ni signos y con artículos "(LA)" al principio."""
    if not text:
        return ""
    text = text.strip()
    article = re.search(r"\((el|la|los|las)\)\s*$", text, re.I)
    if article:
        text = f"{article.group(1)} {text[:article.start()]}"
    text = unicodedata.normalize("NFD", text.lower())
    text = "".join(ch for ch in text if unicodedata.category(ch) != "Mn")
    return re.sub(r"[^a-z0-9]+", " ", text).strip()


def classify(tags: dict) -> str | None:
    """Categoría de un elemento de OSM, o ``None`` si no interesa."""
    if tags.get("place"):
        return None  # pueblos y ciudades (algunos llevan heritage=1 por la UNESCO)
    amenity = tags.get("amenity")
    tourism = tags.get("tourism")
    historic = tags.get("historic")
    natural = tags.get("natural")
    shop = tags.get("shop")

    if amenity == "pharmacy":
        return "pharmacy"
    if amenity == "charging_station":
        return "charging"
    if amenity == "hospital":
        return "health"
    if amenity == "clinic":
        # Solo centros de salud: fuera clínicas dentales, de fisioterapia, etc.
        return "health" if tags.get("healthcare", "clinic") in {"clinic", "centre", "hospital"}             and not re.search(r"dent|fisio|estetic|veterin|optic", normalize(tags.get("name"))) else None
    if tourism == "information":
        return "tourist_info"
    if tourism in {"hotel", "motel"}:
        return "hotel"
    if tourism in {"hostel", "alpine_hut"}:
        return "hostel"
    if tourism in {"guest_house", "chalet"}:
        return "rural"
    if tourism == "apartment":
        return "apartment"
    if tourism in {"camp_site", "caravan_site"}:
        return "camping"
    if amenity in {"restaurant", "cafe", "ice_cream"}:
        return amenity
    if amenity in {"bar", "pub"}:
        return "bar"
    if tourism == "museum":
        return "museum"
    if tourism == "gallery":
        return "gallery"
    if tags.get("leisure") in {"bathing_place", "swimming_area"} or natural == "beach":
        return "bathing"
    if natural == "waterfall" or tags.get("waterway") == "waterfall":
        return "waterfall"
    if natural in {"cave_entrance"}:
        return "cave"
    if natural == "peak":
        return "peak"
    if natural == "gorge":
        return "viewpoint"
    if tags.get("leisure") == "nature_reserve" or tags.get("boundary") == "protected_area":
        return "protected_area"
    if historic in _HERITAGE_HISTORIC:
        return _HERITAGE_HISTORIC[historic]
    if amenity == "place_of_worship":
        return "religious"
    if historic or tags.get("heritage"):
        return "monument"
    if shop in {"gift", "souvenir"}:
        return "gift"
    if shop in {"deli", "farm", "wine", "cheese"}:
        return "local_products"
    if shop == "books":
        return "books"
    if tourism == "viewpoint":
        return "viewpoint"
    if tourism == "attraction":
        return "attraction"
    return None


def element_id(element: dict) -> str:
    return f"{element['type'][0]}{element['id']}"


def coordinates(element: dict) -> tuple[float, float] | None:
    if "lat" in element:
        return element["lat"], element["lon"]
    center = element.get("center")
    if center:
        return center["lat"], center["lon"]
    return None


_INE_PROVINCE = {"06": "BA", "10": "CC"}
_BAND = 0.02  # grados de latitud por franja del índice de segmentos


class Municipality:
    """Límite municipal como conjunto de segmentos (sin ordenar los anillos).

    La regla par-impar del rayo funciona con los segmentos sueltos de todos
    los anillos, exteriores e interiores, así que no hace falta montarlos.
    """

    def __init__(self, element: dict):
        tags = element.get("tags", {})
        self.id = element["id"]
        self.name = tags.get("name", "")
        self.prov = _INE_PROVINCE.get(tags.get("ine:municipio", "")[:2])
        segments = []
        label = None
        for member in element.get("members", []):
            if member.get("role") in {"admin_centre", "label"} and "lat" in member:
                label = (member["lat"], member["lon"])
            geometry = member.get("geometry") or []
            if member.get("type") != "way":
                continue
            for a, b in zip(geometry, geometry[1:]):
                segments.append((a["lat"], a["lon"], b["lat"], b["lon"]))
        lats = [s[0] for s in segments] + [s[2] for s in segments]
        lons = [s[1] for s in segments] + [s[3] for s in segments]
        self.bbox = (min(lats), min(lons), max(lats), max(lons)) if segments else None
        self.bands: dict[int, list[tuple]] = {}
        for seg in segments:
            low, high = sorted((seg[0], seg[2]))
            for band in range(int(low // _BAND), int(high // _BAND) + 1):
                self.bands.setdefault(band, []).append(seg)
        if label is None and self.bbox:
            label = ((self.bbox[0] + self.bbox[2]) / 2, (self.bbox[1] + self.bbox[3]) / 2)
        self.lat, self.lon = label or (0.0, 0.0)

    def contains(self, lat: float, lon: float) -> bool:
        if not self.bbox or not (self.bbox[0] <= lat <= self.bbox[2]
                                 and self.bbox[1] <= lon <= self.bbox[3]):
            return False
        inside = False
        for lat1, lon1, lat2, lon2 in self.bands.get(int(lat // _BAND), ()):
            if (lat1 > lat) != (lat2 > lat):
                cross = lon1 + (lat - lat1) * (lon2 - lon1) / (lat2 - lat1)
                if cross > lon:
                    inside = not inside
        return inside

    def summary(self) -> dict:
        return {"id": self.id, "name": self.name, "prov": self.prov,
                "lat": round(self.lat, 5), "lon": round(self.lon, 5)}


def municipalities(osm: dict) -> dict[int, Municipality]:
    """Municipios de Extremadura con su provincia (código INE)."""
    result = {}
    for element in osm["elements"]:
        muni = Municipality(element)
        if muni.prov and muni.bbox:
            result[muni.id] = muni
    return result


def locate(munis: dict[int, Municipality], lat: float, lon: float) -> Municipality | None:
    return next((m for m in munis.values() if m.contains(lat, lon)), None)


def places_from_osm(osm: dict, munis: dict[int, Municipality]) -> dict[str, dict]:
    """Lugares de OSM dentro de un municipio de Extremadura."""
    places: dict[str, dict] = {}
    for element in osm["elements"]:
        tags = element.get("tags", {})
        category = classify(tags)
        point = coordinates(element)
        if category is None or point is None:
            continue
        key = element_id(element)
        if key in places:
            continue
        current_muni = locate(munis, *point)
        if current_muni is None:
            continue
        name = tags.get("name") or _fallback_name(category, tags)
        if not name:
            continue
        places[key] = {
            "id": key,
            "name": name.strip(),
            "cat": category,
            "group": CATEGORY_GROUP[category],
            "lat": round(point[0], 6),
            "lon": round(point[1], 6),
            "muni": current_muni.name,
            "prov": current_muni.prov,
            "addr": _address(tags),
            "web": _website(tags),
            "phone": tags.get("phone") or tags.get("contact:phone"),
            "hours": tags.get("opening_hours"),
            "wheelchair": tags.get("wheelchair"),
            "qid": tags.get("wikidata") if re.fullmatch(r"Q\d+", tags.get("wikidata", "")) else None,
            "heritage": bool(tags.get("heritage")),
            "src": ["osm"],
        }
    return places


def _fallback_name(category: str, tags: dict) -> str | None:
    if category in {"charging", "pharmacy"}:
        return tags.get("operator") or tags.get("brand")
    return None


def _address(tags: dict) -> str | None:
    street = tags.get("addr:street")
    if not street:
        return None
    number = tags.get("addr:housenumber")
    return f"{street}, {number}" if number else street


def _website(tags: dict) -> str | None:
    url = tags.get("website") or tags.get("contact:website") or tags.get("url")
    if not url:
        return None
    url = url.strip().split(";")[0].strip()
    if not re.match(r"^https?://", url, re.I):
        url = f"https://{url}"
    return url if " " not in url else None


_CLASS_RULES = [
    (re.compile(r"town|village|city|municipality|settlement|hamlet|district|comarca"), None),
    (re.compile(r"museum|gallery"), "museum"),
    (re.compile(r"castle|fortress|\bfort|tower|wall|alcazaba|bastion"), "castle"),
    (re.compile(r"church|chapel|cathedral|monastery|convent|hermitage|basilica|shrine|abbey|synagogue|mosque"), "religious"),
    (re.compile(r"palace|manor|mansion|house|stately"), "palace"),
    (re.compile(r"bridge|aqueduct"), "bridge"),
    (re.compile(r"archaeolog|dolmen|megalith|ruin|villa|roman|necropolis|castro|theatre|amphitheatre|circus|cave painting|rock art"), "archaeology"),
    (re.compile(r"waterfall"), "waterfall"),
    (re.compile(r"cave"), "cave"),
    (re.compile(r"park|reserve|protected|natural monument|biosphere|geopark|nature"), "protected_area"),
]


def classify_wikidata(class_labels: list[str]) -> str | None:
    """Categoría a partir de las clases (P31) en inglés; None si es una población."""
    text = " ".join(class_labels).lower()
    for pattern, category in _CLASS_RULES:
        if pattern.search(text):
            return category
    return "monument"


def _parse_point(wkt: str) -> tuple[float, float] | None:
    match = re.match(r"Point\(([-\d.eE]+) ([-\d.eE]+)\)", wkt or "")
    return (float(match.group(2)), float(match.group(1))) if match else None


def _same_name(a: str, b: str) -> bool:
    a, b = normalize(a), normalize(b)
    if not a or not b:
        return False
    if a == b or (min(len(a), len(b)) >= 6 and (a in b or b in a)):
        return True
    words_a, words_b = set(a.split()) - _STOP, set(b.split()) - _STOP
    if not words_a or not words_b:
        return False
    return len(words_a & words_b) / min(len(words_a), len(words_b)) >= 0.75


_STOP = {"de", "del", "la", "las", "el", "los", "y", "en", "san", "santa", "the", "of"}


def add_wikidata_sights(places: dict[str, dict], rows: list[dict],
                        munis: dict[int, "Municipality"]) -> tuple[int, int]:
    """Cruza los lugares de Wikidata con OSM. Si ya existe en OSM (mismo nombre a
    menos de 400 m) le añade el QID; si no, lo crea con id ``Q…``.
    Devuelve (enlazados, nuevos)."""
    items: dict[str, dict] = {}
    for row in rows:
        qid = row["item"].rsplit("/", 1)[-1]
        item = items.setdefault(qid, {"classes": []})
        for key in ("coord", "labelEs", "labelEn"):
            if row.get(key) and key not in item:
                item[key] = row[key]
        if row.get("classLabel"):
            item["classes"].append(row["classLabel"])

    known_qids = {p["qid"] for p in places.values() if p.get("qid")}
    sights = [p for p in places.values() if p["group"] in {"HERITAGE", "MUSEUMS", "NATURE", "BATHING"}]
    linked = created = 0
    for qid in sorted(items, key=lambda q: int(q[1:])):
        item = items[qid]
        point = _parse_point(item.get("coord", ""))
        name = item.get("labelEs") or item.get("labelEn")
        category = classify_wikidata(item["classes"])
        if qid in known_qids or point is None or not name or category is None:
            continue
        muni = locate(munis, *point)
        # Entradas que son el propio pueblo (conjunto histórico): ya están como municipio
        if muni is None or normalize(name) == normalize(muni.name):
            continue
        match = next((p for p in sights if not p.get("qid")
                      and _distance_m(point, (p["lat"], p["lon"])) < 400
                      and _same_name(name, p["name"])), None)
        if match is not None:
            match["qid"] = qid
            linked += 1
            continue
        places[qid] = {
            "id": qid, "name": name, "cat": category, "group": CATEGORY_GROUP[category],
            "lat": round(point[0], 6), "lon": round(point[1], 6),
            "muni": muni.name, "prov": muni.prov, "addr": None, "web": None, "phone": None,
            "hours": None, "wheelchair": None, "qid": qid, "heritage": False, "src": [],
        }
        created += 1
    return linked, created


def _distance_m(a: tuple[float, float], b: tuple[float, float]) -> float:
    lat = math.radians((a[0] + b[0]) / 2)
    dy = (a[0] - b[0]) * 111_320
    dx = (a[1] - b[1]) * 111_320 * math.cos(lat)
    return math.hypot(dx, dy)


def merge_wikidata(places: dict[str, dict], rows: list[dict],
                   extracts_es: dict[str, str], extracts_en: dict[str, str],
                   images: dict[str, dict]) -> None:
    """Añade textos en español e inglés e imagen con licencia libre."""
    by_qid: dict[str, dict] = {}
    for row in rows:
        qid = row["item"].rsplit("/", 1)[-1]
        merged = by_qid.setdefault(qid, {})
        for key, value in row.items():
            merged.setdefault(key, value)

    for place in places.values():
        data = by_qid.get(place["qid"] or "")
        if not data:
            continue
        place["src"].append("wikidata")
        place["nameEn"] = data.get("labelEn") if data.get("labelEn") != place["name"] else None
        place["descEs"] = extracts_es.get(data.get("wpEs", "")) or _sentence(data.get("descEs"))
        place["descEn"] = extracts_en.get(data.get("wpEn", "")) or _sentence(data.get("descEn"))
        if data.get("wpEs") or data.get("wpEn"):
            place["src"].append("wikipedia")
        place["wpEs"] = data.get("wpEs")
        place["wpEn"] = data.get("wpEn")
        if data.get("heritage"):
            place["heritage"] = True
        if data.get("image"):
            image = images.get(commons_file(data["image"]))
            if image and image.get("thumb") and _FREE_LICENSE.match(image.get("license", "")):
                place["img"] = image["thumb"]
                place["imgPage"] = image["page"]
                place["imgAuthor"] = clean_artist(image.get("artist", "")) or None
                place["imgLicense"] = image["license"]
                place["src"].append("commons")


def commons_file(url: str) -> str:
    return urllib.parse.unquote(url.rsplit("/", 1)[-1]).replace("_", " ")


def clean_artist(value: str) -> str:
    text = html.unescape(_TAG.sub("", value))
    return re.sub(r"\s+", " ", text).strip()[:120]


def _sentence(text: str | None) -> str | None:
    if not text:
        return None
    return text[0].upper() + text[1:] + ("" if text.endswith(".") else ".")


_SLEEP_WORDS = re.compile(
    r"\b(hotel|hostal|hostel|pension|albergue|apartamentos?|casa rural|"
    r"camping|motel|hospederia|parador|h|hr|hs)\b")


def _sleep_key(name: str) -> str:
    return re.sub(r"\s+", " ", _SLEEP_WORDS.sub(" ", normalize(name))).strip()


def match_junta_accommodation(places: dict[str, dict], rows: list[dict]) -> tuple[int, int]:
    """Marca como oficiales los alojamientos de OSM inscritos en el registro
    de la Junta (que no trae coordenadas) cruzando nombre y municipio."""
    index: dict[str, list[dict]] = {}
    for place in places.values():
        if place["group"] == "SLEEP":
            index.setdefault(normalize(place["muni"]), []).append(place)

    matched = 0
    for row in rows:
        candidates = index.get(normalize(row.get("Municipio")), [])
        name = _sleep_key(row.get("Nombre establecimiento", ""))
        if not name:
            continue
        found = next((p for p in candidates if _sleep_key(p["name"]) == name), None)
        if found is None:
            found = next((p for p in candidates
                          if len(name) >= 5 and name in _sleep_key(p["name"])), None)
        if found is None or "junta" in found["src"]:
            continue
        found["src"].append("junta")
        found["official"] = " ".join(
            part for part in (row.get("Tipo recurso"), row.get("Categoría")) if part)
        matched += 1
    return matched, len(rows)


# Peso turístico de cada destino: Mérida (conjunto arqueológico, Patrimonio de la
# Humanidad) es el principal, seguida de Cáceres, Trujillo y Guadalupe
DESTINATION_WEIGHT = {
    "merida": 5, "caceres": 3, "trujillo": 3, "guadalupe": 3, "plasencia": 2,
    "badajoz": 2, "zafra": 1, "hervas": 1, "jerez de los caballeros": 1,
    "coria": 1, "medellin": 1, "alcantara": 1, "olivenza": 1, "llerena": 1,
}

_SIGHT_GROUPS = {"HERITAGE", "MUSEUMS", "NATURE", "BATHING"}


def score(place: dict) -> int:
    """Relevancia turística para ordenar destacados y resolver empates."""
    value = {"HERITAGE": 3, "MUSEUMS": 3, "NATURE": 3, "BATHING": 2}.get(place["group"], 0)
    if place["group"] in _SIGHT_GROUPS:
        value += DESTINATION_WEIGHT.get(normalize(place["muni"]), 0)
    value += 3 if place.get("wpEs") or place.get("wpEn") else 0
    value += 2 if place.get("img") else 0
    value += 2 if place.get("heritage") else 0
    value += 1 if place.get("qid") else 0
    value += 4 if place.get("routeStop") else 0  # iconos de cada destino (paradas de las rutas)
    return value


def resolve_routes(routes: list[dict], places: dict[str, dict]) -> list[dict]:
    """Enlaza cada parada con su lugar por Wikidata, id de OSM o nombre dentro
    del municipio. Si no hay lugar, la parada conserva sus coordenadas."""
    by_qid = {p["qid"]: p for p in places.values() if p.get("qid")}
    by_muni: dict[str, list[dict]] = {}
    for place in places.values():
        by_muni.setdefault(normalize(place["muni"]), []).append(place)
    resolved = []
    for route in routes:
        stops = []
        for stop in route["stops"]:
            place = (by_qid.get(stop.get("qid", "")) or places.get(stop.get("osm", ""))
                     or _find_by_name(by_muni, stop.get("match"), stop.get("muni")))
            entry = {k: v for k, v in stop.items() if k not in {"match", "osm"}}
            if place:
                place["routeStop"] = True
                entry["placeId"] = place["id"]
                entry["lat"], entry["lon"] = place["lat"], place["lon"]
                entry.setdefault("name", place["name"])
                if place.get("nameEn"):
                    entry["nameEn"] = place["nameEn"]
            if "lat" not in entry:
                raise ValueError(f"Parada sin ubicación en {route['id']}: {stop}")
            stops.append(entry)
        resolved.append({**route, "stops": stops})
    return resolved


def _find_by_name(by_muni: dict[str, list[dict]], name: str | None,
                  muni: str | None) -> dict | None:
    if not name or not muni:
        return None
    wanted = normalize(name)
    candidates = [p for p in by_muni.get(normalize(muni), [])
                  if p["group"] not in {"FOOD", "SLEEP", "SHOPS", "SERVICES"}]
    exact = [p for p in candidates if normalize(p["name"]) == wanted]
    partial = [p for p in candidates if wanted in normalize(p["name"])]
    matches = exact or partial
    # Ante varios candidatos, el más relevante y después el de id menor
    return min(matches, key=lambda p: (-score(p), p["id"])) if matches else None


def build_pack(places: dict[str, dict], munis: dict[int, Municipality], routes: list[dict],
               events: list[dict], version: int) -> dict:
    cleaned = []
    for place in sorted(places.values(), key=lambda p: p["id"]):
        item = {key: value for key, value in place.items()
                if value not in (None, "", [], False) and key != "routeStop"}
        item["score"] = score(place)
        item["src"] = sorted(set(place["src"]))
        cleaned.append(item)
    return {
        "schema": SCHEMA_VERSION,
        "version": version,
        "attribution": [
            "© OpenStreetMap contributors (ODbL 1.0)",
            "Wikidata (CC0), Wikipedia (CC BY-SA 4.0), Wikimedia Commons (licencia de cada imagen)",
            "Junta de Extremadura, datos abiertos (CC BY 4.0)",
        ],
        "municipalities": sorted(
            ({k: v for k, v in m.summary().items() if k != "id"} for m in munis.values()),
            key=lambda m: normalize(m["name"])),
        "places": cleaned,
        "routes": routes,
        "events": events,
    }
