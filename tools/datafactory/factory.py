"""Fábrica de datos de ExplorerSaga.

Uso:
    python factory.py fetch [--refresh]       descarga las fuentes en .cache/
    python factory.py build --version 20261110 [--out dist] [--app]

``build`` no usa la red: con la misma caché y versión genera un paquete
idéntico byte a byte. ``--app`` copia además el paquete a los assets de la app.
"""

from __future__ import annotations

import argparse
import datetime as dt
import gzip
import hashlib
import json
import shutil
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent
sys.path.insert(0, str(ROOT))

from datafactory import sources, transform  # noqa: E402
CACHE = ROOT / ".cache"
CURATED = ROOT / "curated"
APP_ASSETS = ROOT.parent.parent / "app" / "src" / "main" / "assets" / "content"


def fetch(refresh: bool) -> None:
    if refresh or not (CACHE / "junta_hoteles.json").exists():
        sources.fetch_junta(CACHE)
    if refresh or not (CACHE / "osm_pois.json").exists():
        sources.fetch_overpass(CACHE)
    if refresh or not (CACHE / "wikidata_sights.json").exists():
        sources.fetch_wikidata_sights(CACHE)
    if refresh or not (CACHE / "wikidata.json").exists():
        places = load_places()
        qids = sorted({p["qid"] for p in places.values() if p.get("qid")})
        sources.fetch_enrichment(CACHE, qids)


def load_places() -> dict[str, dict]:
    """Lugares de OSM más los de Wikidata que no están en OSM, sin enriquecer."""
    munis = transform.municipalities(sources.load_json(CACHE / "osm_municipalities.json"))
    places = transform.places_from_osm(sources.load_json(CACHE / "osm_pois.json"), munis)
    transform.add_wikidata_sights(places, sources.load_json(CACHE / "wikidata_sights.json"), munis)
    return places


def build(version: int, out: Path, copy_to_app: bool) -> dict:
    munis = transform.municipalities(sources.load_json(CACHE / "osm_municipalities.json"))
    places = transform.places_from_osm(sources.load_json(CACHE / "osm_pois.json"), munis)
    linked, created = transform.add_wikidata_sights(
        places, sources.load_json(CACHE / "wikidata_sights.json"), munis)
    print(f"Wikidata: {linked} lugares de OSM enlazados y {created} nuevos")
    transform.merge_wikidata(
        places,
        sources.load_json(CACHE / "wikidata.json"),
        sources.load_json(CACHE / "wikipedia_es.json"),
        sources.load_json(CACHE / "wikipedia_en.json"),
        sources.load_json(CACHE / "commons.json"),
    )
    matched, total = transform.match_junta_accommodation(
        places, sources.load_json(CACHE / "junta_hoteles.json"))
    routes = transform.resolve_routes(
        sources.load_json(CURATED / "routes.json"), places)
    events = sources.load_json(CURATED / "events.json")
    pack = transform.build_pack(places, munis, routes, events, version)

    raw = json.dumps(pack, ensure_ascii=False, sort_keys=True, separators=(",", ":")).encode()
    packed = gzip.compress(raw, compresslevel=9, mtime=0)
    manifest = {
        "schema": transform.SCHEMA_VERSION,
        "version": version,
        "file": "pack.json.gz",
        "sha256": hashlib.sha256(packed).hexdigest(),
        "size": len(packed),
        "places": len(pack["places"]),
        "routes": len(pack["routes"]),
        "events": len(pack["events"]),
    }
    out.mkdir(parents=True, exist_ok=True)
    (out / "pack.json.gz").write_bytes(packed)
    (out / "manifest.json").write_text(json.dumps(manifest, indent=2) + "\n", encoding="utf-8")
    if copy_to_app:
        APP_ASSETS.mkdir(parents=True, exist_ok=True)
        # aapt quita la extensión .gz de los assets: en la app va como .bin (sigue comprimido)
        shutil.copy2(out / "pack.json.gz", APP_ASSETS / "pack.bin")
        shutil.copy2(out / "manifest.json", APP_ASSETS / "manifest.json")

    report(pack, matched, total)
    return manifest


def report(pack: dict, matched: int, total: int) -> None:
    groups: dict[str, int] = {}
    provinces: dict[str, int] = {}
    for place in pack["places"]:
        groups[place["group"]] = groups.get(place["group"], 0) + 1
        provinces[place.get("prov", "?")] = provinces.get(place.get("prov", "?"), 0) + 1
    with_img = sum(1 for p in pack["places"] if "img" in p)
    with_en = sum(1 for p in pack["places"] if "descEn" in p)
    print(f"Lugares: {len(pack['places'])}  municipios: {len(pack['municipalities'])}")
    print(f"Por grupo: {dict(sorted(groups.items()))}")
    print(f"Por provincia: {dict(sorted(provinces.items()))}")
    print(f"Con imagen libre: {with_img}  con descripción en inglés: {with_en}")
    print(f"Alojamientos de la Junta ubicados: {matched}/{total}")
    print(f"Rutas: {len(pack['routes'])}  eventos: {len(pack['events'])}")


def main(argv: list[str]) -> int:
    parser = argparse.ArgumentParser(description=__doc__,
                                     formatter_class=argparse.RawDescriptionHelpFormatter)
    sub = parser.add_subparsers(dest="command", required=True)
    fetch_cmd = sub.add_parser("fetch")
    fetch_cmd.add_argument("--refresh", action="store_true")
    build_cmd = sub.add_parser("build")
    build_cmd.add_argument("--version", type=int,
                           default=int(dt.date.today().strftime("%Y%m%d")))
    build_cmd.add_argument("--out", type=Path, default=ROOT / "dist")
    build_cmd.add_argument("--app", action="store_true")
    args = parser.parse_args(argv)

    if args.command == "fetch":
        fetch(args.refresh)
    else:
        print(json.dumps(build(args.version, args.out, args.app), indent=2))
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
