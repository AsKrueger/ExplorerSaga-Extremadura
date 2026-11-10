package com.alonso.explorersaga.adapters;

import com.alonso.explorersaga.adapters.out.external.OverpassAdapter;
import com.alonso.explorersaga.application.port.out.NormalizedPlaceData;
import com.alonso.explorersaga.exception.ExternalProviderException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OverpassAdapterTest {

    private static final String BASE_URL = "http://overpass.test/api/interpreter";
    private static final String MIRROR_URL = "http://overpass-mirror.test/api/interpreter";
    private static final String EMPTY_RESPONSE = "{\"elements\": []}";

    private RestTemplate restTemplate;
    private MockRestServiceServer server;
    private OverpassAdapter adapter;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        adapter = new OverpassAdapter(restTemplate, BASE_URL);
    }

    @Test
    void fetchTourismPlaces_mapsNodesAndWaysWithCenter() {
        String body = """
                {
                  "elements": [
                    {"type": "node", "id": 1, "lat": 38.91, "lon": -6.34,
                     "tags": {"name": "Bar Central", "amenity": "bar", "addr:street": "Calle Mayor"}},
                    {"type": "way", "id": 2, "center": {"lat": 38.92, "lon": -6.33},
                     "tags": {"name": "Teatro Romano", "historic": "archaeological_site"}},
                    {"type": "node", "id": 3, "lat": 38.93, "lon": -6.32,
                     "tags": {"shop": "books"}},
                    {"type": "way", "id": 4,
                     "tags": {"name": "Sin coordenadas", "tourism": "museum"}}
                  ]
                }
                """;
        server.expect(requestTo(startsWith(BASE_URL)))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        List<NormalizedPlaceData> places = adapter.fetchTourismPlaces();

        assertThat(places).hasSize(2);
        assertThat(places.get(0).externalId()).isEqualTo("node/1");
        assertThat(places.get(0).categoryName()).isEqualTo("bar");
        assertThat(places.get(0).address()).isEqualTo("Calle Mayor");
        assertThat(places.get(1).externalId()).isEqualTo("way/2");
        assertThat(places.get(1).latitude()).isEqualTo(38.92);
        assertThat(places.get(1).longitude()).isEqualTo(-6.33);
        assertThat(places.get(1).categoryName()).isEqualTo("monumento");
        server.verify();
    }

    @Test
    void fetchTourismPlaces_mapsTagsToSpanishCategoriesAndDiscardsNonVisitorPlaces() {
        String body = """
                {
                  "elements": [
                    {"type": "node", "id": 1, "lat": 38.9, "lon": -6.3, "tags": {"name": "MNAR", "tourism": "museum"}},
                    {"type": "node", "id": 2, "lat": 38.9, "lon": -6.3,
                     "tags": {"name": "Basílica", "amenity": "place_of_worship", "historic": "church"}},
                    {"type": "node", "id": 3, "lat": 38.9, "lon": -6.3, "tags": {"name": "Mirador", "tourism": "viewpoint"}},
                    {"type": "node", "id": 4, "lat": 38.9, "lon": -6.3, "tags": {"name": "Moda", "shop": "shoes"}},
                    {"type": "node", "id": 5, "lat": 38.9, "lon": -6.3, "tags": {"name": "Horno", "shop": "bakery"}},
                    {"type": "node", "id": 6, "lat": 38.9, "lon": -6.3, "tags": {"name": "Bazar", "shop": "variety_store"}},
                    {"type": "node", "id": 7, "lat": 38.9, "lon": -6.3, "tags": {"name": "Hotel Ejemplo", "tourism": "hotel"}},
                    {"type": "node", "id": 8, "lat": 38.9, "lon": -6.3, "tags": {"name": "Oficina", "tourism": "information"}},
                    {"type": "node", "id": 9, "lat": 38.9, "lon": -6.3, "tags": {"name": "Taller", "shop": "car_repair"}},
                    {"type": "node", "id": 10, "lat": 38.9, "lon": -6.3, "tags": {"name": "Apuestas", "shop": "bookmaker"}}
                  ]
                }
                """;
        server.expect(requestTo(startsWith(BASE_URL)))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        Map<String, String> categoryByName = adapter.fetchTourismPlaces().stream()
                .collect(Collectors.toMap(NormalizedPlaceData::name, NormalizedPlaceData::categoryName));

        assertThat(categoryByName).containsExactlyInAnyOrderEntriesOf(Map.of(
                "MNAR", "museo",
                "Basílica", "iglesia",
                "Mirador", "monumento",
                "Moda", "ropa",
                "Horno", "tienda_alimentos",
                "Bazar", "tienda_general"
        ));
        server.verify();
    }

    @Test
    void fetchTourismPlaces_sendsQueryEncodedOnlyOnce() {
        server.expect(requestTo(startsWith(BASE_URL)))
                .andExpect(request -> assertThat(request.getURI().getRawQuery())
                        .startsWith("data=")
                        .contains("%5Bout%3Ajson%5D")
                        .doesNotContain("%25"))
                .andRespond(withSuccess(EMPTY_RESPONSE, MediaType.APPLICATION_JSON));

        assertThat(adapter.fetchTourismPlaces()).isEmpty();
        server.verify();
    }

    @Test
    void fetchTourismPlaces_fallsBackToNextInstanceWhenPrimaryIsBusy() {
        adapter = new OverpassAdapter(restTemplate, List.of(BASE_URL, MIRROR_URL), 1, 0);
        server.expect(requestTo(startsWith(BASE_URL))).andRespond(withStatus(HttpStatus.BAD_GATEWAY));
        server.expect(requestTo(startsWith(MIRROR_URL)))
                .andRespond(withSuccess(EMPTY_RESPONSE, MediaType.APPLICATION_JSON));

        assertThat(adapter.fetchTourismPlaces()).isEmpty();
        server.verify();
    }

    @Test
    void fetchTourismPlaces_retriesInAnotherRoundBeforeGivingUp() {
        adapter = new OverpassAdapter(restTemplate, List.of(BASE_URL), 2, 0);
        server.expect(requestTo(startsWith(BASE_URL))).andRespond(withStatus(HttpStatus.GATEWAY_TIMEOUT));
        server.expect(requestTo(startsWith(BASE_URL)))
                .andRespond(withSuccess(EMPTY_RESPONSE, MediaType.APPLICATION_JSON));

        assertThat(adapter.fetchTourismPlaces()).isEmpty();
        server.verify();
    }

    @Test
    void fetchTourismPlaces_throwsWhenEveryAttemptFails() {
        adapter = new OverpassAdapter(restTemplate, List.of(BASE_URL, MIRROR_URL), 2, 0);
        // Las peticiones alternan entre instancias en cada ronda
        server = MockRestServiceServer.bindTo(restTemplate).ignoreExpectOrder(true).build();
        server.expect(ExpectedCount.times(2), requestTo(startsWith(BASE_URL))).andRespond(withServerError());
        server.expect(ExpectedCount.times(2), requestTo(startsWith(MIRROR_URL))).andRespond(withServerError());

        assertThatThrownBy(() -> adapter.fetchTourismPlaces())
                .isInstanceOf(ExternalProviderException.class)
                .hasMessageContaining("Overpass API no está disponible");
        server.verify();
    }
}
