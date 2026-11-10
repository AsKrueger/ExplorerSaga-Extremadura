import json
import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

from datafactory import transform  # noqa: E402

def square(lat0, lon0, lat1, lon1, hole=None):
    """Relación con un anillo exterior partido en dos vías y un hueco opcional."""
    def way(points, role="outer"):
        return {"type": "way", "role": role,
                "geometry": [{"lat": a, "lon": b} for a, b in points]}
    members = [way([(lat0, lon0), (lat0, lon1), (lat1, lon1)]),
               way([(lat1, lon1), (lat1, lon0), (lat0, lon0)])]
    if hole:
        h0, g0, h1, g1 = hole
        members.append(way([(h0, g0), (h0, g1), (h1, g1), (h1, g0), (h0, g0)], "inner"))
    return members


MUNIS_OSM = {"elements": [
    {"type": "relation", "id": 10, "tags": {"name": "Mérida", "ine:municipio": "06083"},
     "members": square(38.8, -6.5, 39.0, -6.2, hole=(38.95, -6.45, 38.99, -6.40))},
    {"type": "relation", "id": 20, "tags": {"name": "Trujillo", "ine:municipio": "10195"},
     "members": square(39.3, -6.0, 39.6, -5.7)},
    {"type": "relation", "id": 30, "tags": {"name": "Fuera", "ine:municipio": "45001"},
     "members": square(40.0, -4.0, 40.1, -3.9)},
]}

POIS_OSM = {"elements": [
    {"type": "way", "id": 5, "center": {"lat": 38.9157, "lon": -6.3384},
     "tags": {"historic": "archaeological_site", "name": "Teatro Romano", "wikidata": "Q1046374"}},
    {"type": "node", "id": 6, "lat": 38.91, "lon": -6.34,
     "tags": {"tourism": "hotel", "name": "Hotel Ilunion Mérida Palace", "website": "ilunion.com"}},
    {"type": "node", "id": 7, "lat": 38.91, "lon": -6.34, "tags": {"amenity": "bench"}},
    {"type": "node", "id": 8, "lat": 38.91, "lon": -6.34, "tags": {"amenity": "restaurant"}},
    {"type": "way", "id": 5, "center": {"lat": 39.5, "lon": -5.8},
     "tags": {"historic": "archaeological_site", "name": "Teatro Romano"}},
    {"type": "node", "id": 9, "lat": 39.46, "lon": -5.88,
     "tags": {"amenity": "charging_station", "operator": "Iberdrola"}},
    {"type": "node", "id": 11, "lat": 38.97, "lon": -6.42,
     "tags": {"tourism": "museum", "name": "En el hueco"}},
    {"type": "node", "id": 12, "lat": 40.05, "lon": -3.95,
     "tags": {"tourism": "museum", "name": "Fuera de Extremadura"}},
]}


def sample_places():
    munis = transform.municipalities(MUNIS_OSM)
    return transform.places_from_osm(POIS_OSM, munis), munis


class NormalizeTest(unittest.TestCase):
    def test_removes_accents_case_and_symbols(self):
        self.assertEqual(transform.normalize("  Cáceres-Monumental! "), "caceres monumental")

    def test_moves_trailing_article(self):
        self.assertEqual(transform.normalize("ALBUERA (LA)"), "la albuera")

    def test_empty(self):
        self.assertEqual(transform.normalize(None), "")


class ClassifyTest(unittest.TestCase):
    def test_categories(self):
        cases = {
            "museum": {"tourism": "museum"},
            "castle": {"historic": "castle"},
            "bathing": {"leisure": "bathing_place"},
            "rural": {"tourism": "guest_house"},
            "bar": {"amenity": "pub"},
            "local_products": {"shop": "cheese"},
            "pharmacy": {"amenity": "pharmacy", "historic": "building"},
            "religious": {"amenity": "place_of_worship"},
        }
        for expected, tags in cases.items():
            self.assertEqual(transform.classify(tags), expected, tags)

    def test_ignores_irrelevant(self):
        self.assertIsNone(transform.classify({"amenity": "bench"}))
        self.assertIsNone(transform.classify({"place": "city", "heritage": "1"}))
        self.assertIsNone(transform.classify({"amenity": "clinic", "healthcare": "dentist", "name": "X"}))
        self.assertIsNone(transform.classify({"amenity": "clinic", "name": "dentalestetic"}))
        self.assertEqual(transform.classify({"amenity": "clinic", "name": "Centro de Salud"}), "health")

    def test_every_category_has_group(self):
        for group in transform.CATEGORY_GROUP.values():
            self.assertIn(group, {"HERITAGE", "MUSEUMS", "NATURE", "BATHING", "FOOD",
                                  "SLEEP", "SHOPS", "SERVICES"})


class PlacesFromOsmTest(unittest.TestCase):
    def test_assigns_municipality_province_and_dedupes(self):
        places, _ = sample_places()
        theatre = places["w5"]
        self.assertEqual(theatre["muni"], "Mérida")
        self.assertEqual(theatre["prov"], "BA")
        self.assertEqual(theatre["lat"], 38.9157)  # primera aparición
        self.assertEqual(theatre["group"], "HERITAGE")
        self.assertEqual(theatre["qid"], "Q1046374")

    def test_point_in_polygon_respects_holes_and_region(self):
        places, munis = sample_places()
        self.assertNotIn("n11", places)  # dentro del hueco del municipio
        self.assertNotIn("n12", places)  # municipio de fuera de Extremadura
        self.assertEqual(sorted(munis), [10, 20])

    def test_skips_unnamed_but_names_chargers_by_operator(self):
        places, _ = sample_places()
        self.assertNotIn("n7", places)
        self.assertNotIn("n8", places)
        self.assertEqual(places["n9"]["name"], "Iberdrola")
        self.assertEqual(places["n9"]["prov"], "CC")

    def test_website_gets_scheme(self):
        places, _ = sample_places()
        self.assertEqual(places["n6"]["web"], "https://ilunion.com")


class EnrichmentTest(unittest.TestCase):
    def test_merge_wikidata_adds_texts_and_licensed_image(self):
        places, _ = sample_places()
        rows = [{"item": "http://www.wikidata.org/entity/Q1046374", "labelEn": "Roman Theatre",
                 "wpEs": "Teatro romano de Mérida", "wpEn": "Roman Theatre (Mérida)",
                 "image": "http://commons.wikimedia.org/wiki/Special:FilePath/Teatro%20M%C3%A9rida.jpg",
                 "heritage": "http://www.wikidata.org/entity/Q23712"}]
        images = {"Teatro Mérida.jpg": {"thumb": "https://upload/t.jpg", "page": "https://c/p",
                                        "artist": "<a href='x'>Ana</a>", "license": "CC BY-SA 4.0"}}
        transform.merge_wikidata(places, rows, {"Teatro romano de Mérida": "Texto."},
                                 {"Roman Theatre (Mérida)": "Text."}, images)
        theatre = places["w5"]
        self.assertEqual(theatre["nameEn"], "Roman Theatre")
        self.assertEqual(theatre["descEs"], "Texto.")
        self.assertEqual(theatre["descEn"], "Text.")
        self.assertEqual(theatre["imgAuthor"], "Ana")
        self.assertEqual(theatre["imgLicense"], "CC BY-SA 4.0")
        self.assertTrue(theatre["heritage"])
        self.assertIn("commons", theatre["src"])

    def test_rejects_image_without_free_license(self):
        places, _ = sample_places()
        rows = [{"item": "http://www.wikidata.org/entity/Q1046374",
                 "image": "http://commons.wikimedia.org/wiki/Special:FilePath/A.jpg"}]
        images = {"A.jpg": {"thumb": "t", "page": "p", "artist": "", "license": "Unknown"}}
        transform.merge_wikidata(places, rows, {}, {}, images)
        self.assertNotIn("img", places["w5"])

    def test_junta_match_by_municipality_and_name(self):
        places, _ = sample_places()
        rows = [{"Municipio": "MERIDA", "Nombre establecimiento": "ILUNION MÉRIDA PALACE",
                 "Tipo recurso": "Hotel", "Categoría": "4 Estrellas"},
                {"Municipio": "TRUJILLO", "Nombre establecimiento": "ILUNION MÉRIDA PALACE"}]
        matched, total = transform.match_junta_accommodation(places, rows)
        self.assertEqual((matched, total), (1, 2))
        self.assertEqual(places["n6"]["official"], "Hotel 4 Estrellas")
        self.assertIn("junta", places["n6"]["src"])


class PackTest(unittest.TestCase):
    def test_routes_resolve_by_qid_and_fail_without_location(self):
        places, _ = sample_places()
        routes = [{"id": "r", "stops": [{"qid": "Q1046374"}, {"name": "X", "lat": 1, "lon": 2}]}]
        resolved = transform.resolve_routes(routes, places)
        self.assertEqual(resolved[0]["stops"][0]["placeId"], "w5")
        by_name = transform.resolve_routes(
            [{"id": "n", "stops": [{"match": "teatro", "muni": "MERIDA", "lat": 0, "lon": 0}]}], places)
        stop = by_name[0]["stops"][0]
        self.assertEqual((stop["placeId"], stop["lat"]), ("w5", 38.9157))
        self.assertNotIn("match", stop)
        with self.assertRaises(ValueError):
            transform.resolve_routes([{"id": "bad", "stops": [{"qid": "Q1"}]}], places)

    def test_merida_outranks_other_destinations(self):
        base = {"group": "HERITAGE", "img": "x", "wpEs": "x", "heritage": True, "qid": "Q1"}
        merida = transform.score({**base, "muni": "Mérida"})
        caceres = transform.score({**base, "muni": "Cáceres"})
        badajoz = transform.score({**base, "muni": "Badajoz"})
        self.assertGreater(merida, caceres)
        self.assertGreater(merida, badajoz)
        # Los restaurantes no suben por estar en un destino importante
        self.assertEqual(transform.score({"group": "FOOD", "muni": "Mérida"}), 0)

    def test_build_is_deterministic(self):
        def build():
            places, munis = sample_places()
            pack = transform.build_pack(places, munis, [], [], 20261110)
            return json.dumps(pack, ensure_ascii=False, sort_keys=True)

        first, second = build(), build()
        self.assertEqual(first, second)
        pack = json.loads(first)
        self.assertEqual([p["id"] for p in pack["places"]], sorted(p["id"] for p in pack["places"]))
        self.assertNotIn("phone", pack["places"][0])  # sin campos vacíos
        self.assertEqual(pack["municipalities"][0]["name"], "Mérida")


if __name__ == "__main__":
    unittest.main()
