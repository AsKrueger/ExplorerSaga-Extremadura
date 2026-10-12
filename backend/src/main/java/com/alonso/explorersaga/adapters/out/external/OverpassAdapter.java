package com.alonso.explorersaga.adapters.out.external;

import com.alonso.explorersaga.application.port.out.NormalizedPlaceData;
import com.alonso.explorersaga.application.port.out.TourismDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class OverpassAdapter implements TourismDataProvider {

    private static final Logger logger = LoggerFactory.getLogger(OverpassAdapter.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public OverpassAdapter(
            RestTemplate restTemplate,
            @Value("${explorersaga.providers.overpass.base-url:https://overpass-api.de/api/interpreter}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    @Override
    public List<NormalizedPlaceData> fetchTourismPlaces() {
        String query = """
                [out:json][timeout:25];
                (
                  node["tourism"="attraction"](38.88,-6.36,38.95,-6.30);
                  way["tourism"="attraction"](38.88,-6.36,38.95,-6.30);
                  node["historic"="monument"](38.88,-6.36,38.95,-6.30);
                  way["historic"="monument"](38.88,-6.36,38.95,-6.30);
                  node["amenity"="place_of_worship"](38.88,-6.36,38.95,-6.30);
                );
                out body;
                >;
                out skel qt;
                """;

        try {
            String requestUrl = baseUrl + "?data=" + URLEncoder.encode(query, StandardCharsets.UTF_8);
            OverpassResponseDto response = restTemplate.getForObject(requestUrl, OverpassResponseDto.class);

            if (response == null || response.getElements() == null) {
                return Collections.emptyList();
            }

            List<NormalizedPlaceData> normalizedList = new ArrayList<>();
            for (OverpassElementDto element : response.getElements()) {
                if (element.getTags() == null || !element.getTags().containsKey("name")) {
                    continue;
                }
                Double lat = element.getLat();
                Double lon = element.getLon();
                if (lat == null || lon == null) {
                    continue;
                }

                Map<String, String> tags = element.getTags();
                String name = tags.get("name");
                String externalId = element.getType() + "/" + element.getId();
                String description = tags.get("description");
                String address = tags.get("addr:street");
                if (address == null) {
                    address = tags.get("address");
                }

                String categoryName = "Monument";
                if (tags.containsKey("historic")) {
                    categoryName = "Historical Monument";
                } else if (tags.containsKey("tourism")) {
                    categoryName = "Tourism Attraction";
                } else if (tags.containsKey("amenity") && "place_of_worship".equals(tags.get("amenity"))) {
                    categoryName = "Religious Site";
                }

                normalizedList.add(new NormalizedPlaceData(
                        externalId,
                        "OSM_OVERPASS",
                        name,
                        description,
                        address,
                        lat,
                        lon,
                        categoryName
                ));
            }
            return normalizedList;
        } catch (Exception e) {
            logger.error("Failed to fetch tourism places from Overpass API: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
