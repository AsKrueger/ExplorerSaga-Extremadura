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

    def test_commons_file_decodes_url(self):
        url = "http://commons.wikimedia.org/wiki/Special:FilePath/Puente_romano%20de%20M%C3%A9rida.jpg"
        self.assertEqual(transform.commons_file(url), "Puente romano de Mérida.jpg")

    def test_clean_artist_strips_html_and_limits_length(self):
        raw = "<a href='//commons'>Jos&eacute;\n  P&eacute;rez</a> <span>(talk)</span>"
        self.assertEqual(transform.clean_artist(raw), "José Pérez (talk)")
        self.assertEqual(len(transform.clean_artist("x" * 300)), 120)
        self.assertEqual(transform.clean_artist("<b></b>"), "")

    def test_short_description_becomes_sentence(self):
        sentence = transform._sentence
        self.assertEqual(sentence("puente romano sobre el Guadiana"), "Puente romano sobre el Guadiana.")
        self.assertEqual(sentence("Castillo."), "Castillo.")
        self.assertIsNone(sentence(""))
        self.assertIsNone(sentence(None))

    def test_junta_match_by_municipality_and_name(self):
        places, _ = sample_places()
        rows = [{"Municipio": "MERIDA", "Nombre establecimiento": "ILUNION MÉRIDA PALACE",
                 "Tipo recurso": "Hotel", "Categoría": "4 Estrellas"},
                {"Municipio": "TRUJILLO", "Nombre establecimiento": "ILUNION MÉRIDA PALACE"}]
        matched, total = transform.match_junta_accommodation(places, rows)
        self.assertEqual((matched, total), (1, 2))
        self.assertEqual(places["n6"]["official"], "Hotel 4 Estrellas")
        self.assertIn("junta", places["n6"]["src"])


def point(lat, lon):
    """Coordenada en el formato WKT que devuelve el SPARQL de Wikidata."""
    return f"Point({lon} {lat})"


def wd_row(qid, label, lat, lon, cls, **extra):
    return {"item": f"http://www.wikidata.org/entity/{qid}", "labelEs": label,
            "coord": point(lat, lon), "classLabel": cls, **extra}


class WikidataSightsTest(unittest.TestCase):
    def places_with_castle(self):
        munis = transform.municipalities(MUNIS_OSM)
        castle = {"type": "way", "id": 40, "center": {"lat": 39.4625, "lon": -5.8822},
                  "tags": {"historic": "castle", "name": "Castillo de Trujillo"}}
        osm = {"elements": POIS_OSM["elements"] + [castle]}
        return transform.places_from_osm(osm, munis), munis

    def test_links_osm_sight_with_same_name_nearby(self):
        places, munis = self.places_with_castle()
        rows = [wd_row("Q500", "Castillo de Trujillo", 39.4638, -5.8822, "castle")]
        self.assertEqual(transform.add_wikidata_sights(places, rows, munis), (1, 0))
        self.assertEqual(places["w40"]["qid"], "Q500")
        self.assertNotIn("Q500", places)

    def test_creates_sight_when_osm_one_is_too_far(self):
        places, munis = self.places_with_castle()
        rows = [wd_row("Q500", "Castillo de Trujillo", 39.4725, -5.8822, "castle")]
        self.assertEqual(transform.add_wikidata_sights(places, rows, munis), (0, 1))
        self.assertIsNone(places["w40"]["qid"])
        self.assertEqual(places["Q500"]["cat"], "castle")

    def test_new_sight_gets_municipality_group_and_rounded_coordinates(self):
        places, munis = sample_places()
        rows = [wd_row("Q600", "Casa del Mitreo", 38.90991234, -6.34811234, "building"),
                wd_row("Q600", "Mithraeum House", 38.90991234, -6.34811234, "archaeological site")]
        self.assertEqual(transform.add_wikidata_sights(places, rows, munis), (0, 1))
        sight = places["Q600"]
        self.assertEqual(sight["name"], "Casa del Mitreo")
        self.assertEqual((sight["cat"], sight["group"]), ("archaeology", "HERITAGE"))
        self.assertEqual((sight["muni"], sight["prov"]), ("Mérida", "BA"))
        self.assertEqual((sight["lat"], sight["lon"]), (38.909912, -6.348112))
        self.assertEqual((sight["id"], sight["qid"], sight["src"]), ("Q600", "Q600", []))

    def test_uses_english_label_when_spanish_is_missing(self):
        places, munis = sample_places()
        row = wd_row("Q601", None, 38.92, -6.35, "museum", labelEn="Some Museum")
        transform.add_wikidata_sights(places, [row], munis)
        self.assertEqual(places["Q601"]["name"], "Some Museum")

    def test_skips_known_towns_outside_and_incomplete_items(self):
        places, munis = sample_places()
        before = set(places)
        rows = [
            wd_row("Q1046374", "Teatro romano de Mérida", 38.9157, -6.3384, "theatre"),
            wd_row("Q700", "Trujillo", 39.46, -5.88, "castle"),
            wd_row("Q701", "Aldea", 39.45, -5.85, "village"),
            wd_row("Q702", "Museo lejano", 40.05, -3.95, "museum"),
            wd_row("Q703", "Museo en el hueco", 38.97, -6.42, "museum"),
            wd_row("Q704", "Sin nombre", 38.92, -6.35, "museum", labelEs=None),
            {"item": "http://www.wikidata.org/entity/Q705", "labelEs": "Sin coordenadas",
             "classLabel": "museum"},
        ]
        self.assertEqual(transform.add_wikidata_sights(places, rows, munis), (0, 0))
        self.assertEqual(set(places), before)

    def test_classify_wikidata(self):
        self.assertIsNone(transform.classify_wikidata(["village", "castle"]))
        self.assertEqual(transform.classify_wikidata(["art museum"]), "museum")
        self.assertEqual(transform.classify_wikidata(["Roman bridge"]), "bridge")
        self.assertEqual(transform.classify_wikidata(["dolmen"]), "archaeology")
        self.assertEqual(transform.classify_wikidata(["natural park"]), "protected_area")
        self.assertEqual(transform.classify_wikidata(["fountain"]), "monument")
        self.assertEqual(transform.classify_wikidata([]), "monument")

    def test_same_name(self):
        same = transform._same_name
        self.assertTrue(same("Iglesia de Santa María", "IGLESIA DE SANTA MARIA LA MAYOR"))
        self.assertTrue(same("Ermita de San Juan", "Ermita San Juan Bautista"))
        self.assertTrue(same("Foro", "Foro romano"))
        self.assertFalse(same("Iglesia de San Juan", "Iglesia de San Pedro"))
        self.assertFalse(same("San", "Santa"))
        self.assertFalse(same("", "Castillo"))


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
