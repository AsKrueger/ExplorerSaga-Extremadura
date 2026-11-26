"""Casos límite del cruce con el registro de la Junta, de la resolución de
paradas de las rutas y de la limpieza del paquete."""
import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
sys.path.insert(0, str(Path(__file__).resolve().parent))

from datafactory import transform  # noqa: E402
from test_transform import sample_places  # noqa: E402


def sight(pid, name, muni="Mérida", group="HERITAGE", **extra):
    return {"id": pid, "name": name, "muni": muni, "group": group,
            "lat": 38.9, "lon": -6.3, "src": ["osm"], **extra}


class JuntaAccommodationEdgeCasesTest(unittest.TestCase):
    def test_ignores_accommodation_words_when_comparing_names(self):
        places, _ = sample_places()
        rows = [{"Municipio": "Mérida", "Nombre establecimiento": "HOTEL ILUNION MERIDA PALACE",
                 "Tipo recurso": "Hotel"}]
        self.assertEqual(transform.match_junta_accommodation(places, rows), (1, 1))
        self.assertEqual(places["n6"]["official"], "Hotel")  # sin categoría, solo el tipo

    def test_partial_name_needs_at_least_five_characters(self):
        places, _ = sample_places()
        short = [{"Municipio": "MERIDA", "Nombre establecimiento": "Ilun"}]
        self.assertEqual(transform.match_junta_accommodation(places, short), (0, 1))
        self.assertNotIn("junta", places["n6"]["src"])

        partial = [{"Municipio": "MERIDA", "Nombre establecimiento": "Hotel Ilunion"}]
        self.assertEqual(transform.match_junta_accommodation(places, partial), (1, 1))
        self.assertIn("junta", places["n6"]["src"])

    def test_counts_each_place_once_and_skips_names_made_of_generic_words(self):
        places, _ = sample_places()
        rows = [{"Municipio": "MERIDA", "Nombre establecimiento": "ILUNION MERIDA PALACE"},
                {"Municipio": "MERIDA", "Nombre establecimiento": "Ilunion Mérida Palace"},
                {"Municipio": "MERIDA", "Nombre establecimiento": "Hotel"},
                {"Municipio": "MERIDA"}]
        self.assertEqual(transform.match_junta_accommodation(places, rows), (1, 4))
        self.assertEqual(places["n6"]["src"].count("junta"), 1)

    def test_only_accommodation_places_are_candidates(self):
        places, _ = sample_places()
        rows = [{"Municipio": "MERIDA", "Nombre establecimiento": "Teatro Romano"}]
        self.assertEqual(transform.match_junta_accommodation(places, rows), (0, 1))
        self.assertNotIn("official", places["w5"])


class RouteResolutionTest(unittest.TestCase):
    def test_links_by_osm_id_and_copies_place_data(self):
        places, _ = sample_places()
        places["w5"]["nameEn"] = "Roman Theatre"
        routes = [{"id": "merida", "title": "Mérida romana",
                   "stops": [{"osm": "w5", "minutes": 60}]}]
        stop = transform.resolve_routes(routes, places)[0]["stops"][0]
        self.assertEqual(stop, {"minutes": 60, "placeId": "w5", "lat": 38.9157, "lon": -6.3384,
                                "name": "Teatro Romano", "nameEn": "Roman Theatre"})
        self.assertTrue(places["w5"]["routeStop"])

    def test_keeps_route_fields_and_custom_stop_name(self):
        places, _ = sample_places()
        routes = [{"id": "r", "title": "Ruta", "stops": [{"qid": "Q1046374", "name": "Teatro"}]}]
        resolved = transform.resolve_routes(routes, places)[0]
        self.assertEqual(resolved["title"], "Ruta")
        self.assertEqual(resolved["stops"][0]["name"], "Teatro")
        self.assertNotIn("nameEn", resolved["stops"][0])

    def test_name_match_ignores_services_and_keeps_own_coordinates(self):
        places, _ = sample_places()
        routes = [{"id": "r", "stops": [{"match": "ilunion", "muni": "Mérida",
                                         "name": "Parada", "lat": 1.0, "lon": 2.0}]}]
        stop = transform.resolve_routes(routes, places)[0]["stops"][0]
        self.assertEqual(stop, {"muni": "Mérida", "name": "Parada", "lat": 1.0, "lon": 2.0})
        self.assertNotIn("routeStop", places["n6"])

    def test_name_match_needs_name_and_municipality(self):
        places, _ = sample_places()
        by_muni = {"merida": list(places.values())}
        self.assertIsNone(transform._find_by_name(by_muni, None, "Mérida"))
        self.assertIsNone(transform._find_by_name(by_muni, "teatro", None))
        self.assertIsNone(transform._find_by_name(by_muni, "teatro", "Trujillo"))


class FindByNameRankingTest(unittest.TestCase):
    def by_muni(self, *places):
        return {"merida": list(places)}

    def test_exact_name_wins_over_more_relevant_partial_match(self):
        exact = sight("n2", "Alcazaba")
        partial = sight("n1", "Alcazaba árabe", img="x", wpEs="x", heritage=True)
        found = transform._find_by_name(self.by_muni(partial, exact), "alcazaba", "Merida")
        self.assertIs(found, exact)

    def test_highest_score_then_lowest_id_among_partial_matches(self):
        plain = sight("n1", "Puente romano del Guadiana")
        famous = sight("n9", "Puente romano del Albarregas", img="x")
        self.assertIs(transform._find_by_name(self.by_muni(plain, famous), "puente romano", "Mérida"),
                      famous)

        twin_a = sight("n7", "Puerta norte")
        twin_b = sight("n3", "Puerta sur")
        self.assertIs(transform._find_by_name(self.by_muni(twin_a, twin_b), "puerta", "Mérida"),
                      twin_b)


class BuildPackCleanupTest(unittest.TestCase):
    def test_drops_internal_flags_and_dedupes_sources(self):
        places, munis = sample_places()
        places["w5"]["routeStop"] = True
        places["w5"]["src"] = ["wikidata", "osm", "wikidata"]
        pack = transform.build_pack(places, munis, [], [], 1)
        theatre = next(p for p in pack["places"] if p["id"] == "w5")
        self.assertNotIn("routeStop", theatre)
        self.assertEqual(theatre["src"], ["osm", "wikidata"])
        self.assertEqual(theatre["score"], transform.score(places["w5"]))
        self.assertTrue(all("id" not in m for m in pack["municipalities"]))
        self.assertEqual([m["name"] for m in pack["municipalities"]], ["Mérida", "Trujillo"])
        self.assertEqual(pack["schema"], transform.SCHEMA_VERSION)
        self.assertEqual(len(pack["attribution"]), 3)


if __name__ == "__main__":
    unittest.main()
