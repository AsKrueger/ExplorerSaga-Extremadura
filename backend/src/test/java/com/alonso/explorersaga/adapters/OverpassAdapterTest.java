package com.alonso.explorersaga.adapters;

import com.alonso.explorersaga.adapters.out.external.OverpassAdapter;
import com.alonso.explorersaga.application.port.out.NormalizedPlaceData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.hamcrest.Matchers.startsWith;

class OverpassAdapterTest {

    private static final String BASE_URL = "http://overpass.test/api/interpreter";

    private MockRestServiceServer server;
    private OverpassAdapter adapter;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
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
        assertThat(places.get(1).categoryName()).isEqualTo("Historical Monument");
        server.verify();
    }

    @Test
    void fetchTourismPlaces_sendsQueryEncodedOnlyOnce() {
        server.expect(requestTo(startsWith(BASE_URL)))
                .andExpect(request -> assertThat(request.getURI().getRawQuery())
                        .startsWith("data=")
                        .contains("%5Bout%3Ajson%5D")
                        .doesNotContain("%25"))
                .andRespond(withSuccess("{\"elements\": []}", MediaType.APPLICATION_JSON));

        assertThat(adapter.fetchTourismPlaces()).isEmpty();
        server.verify();
    }

    @Test
    void fetchTourismPlaces_returnsEmptyListWhenProviderFails() {
        server.expect(requestTo(startsWith(BASE_URL))).andRespond(withServerError());

        assertThat(adapter.fetchTourismPlaces()).isEmpty();
        server.verify();
    }
}
