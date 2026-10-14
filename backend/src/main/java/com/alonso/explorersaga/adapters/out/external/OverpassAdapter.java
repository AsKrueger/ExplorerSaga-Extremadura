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
                  node["tourism"](38.88,-6.36,38.95,-6.30);
                  way["tourism"](38.88,-6.36,38.95,-6.30);
                  node["historic"](38.88,-6.36,38.95,-6.30);
                  way["historic"](38.88,-6.36,38.95,-6.30);
                  node["amenity"~"place_of_worship|restaurant|cafe|bar|pub|ice_cream"](38.88,-6.36,38.95,-6.30);
                  node["shop"](38.88,-6.36,38.95,-6.30);
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

                String categoryName = mapTagsToCategory(tags);

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

    private String mapTagsToCategory(Map<String, String> tags) {
        if (tags.containsKey("shop")) {
            String shopType = tags.get("shop");
            if ("books".equalsIgnoreCase(shopType)) {
                return "libreria";
            } else if ("gift".equalsIgnoreCase(shopType) || "souvenir".equalsIgnoreCase(shopType)) {
                return "souvenir";
            } else if ("supermarket".equalsIgnoreCase(shopType) || "convenience".equalsIgnoreCase(shopType)) {
                return "supermercado";
            } else if ("food".equalsIgnoreCase(shopType) || "bakery".equalsIgnoreCase(shopType) || "deli".equalsIgnoreCase(shopType) || "greengrocer".equalsIgnoreCase(shopType) || "butcher".equalsIgnoreCase(shopType)) {
                return "tienda_alimentos";
            } else if ("clothes".equalsIgnoreCase(shopType) || "fashion".equalsIgnoreCase(shopType)) {
                return "ropa";
            }
            return "tiendasGenerales";
        }

        if (tags.containsKey("amenity")) {
            String amenityType = tags.get("amenity");
            if ("restaurant".equalsIgnoreCase(amenityType)) {
                return "restaurante";
            } else if ("cafe".equalsIgnoreCase(amenityType)) {
                return "cafeteria";
            } else if ("bar".equalsIgnoreCase(amenityType) || "pub".equalsIgnoreCase(amenityType)) {
                return "bar";
            } else if ("ice_cream".equalsIgnoreCase(amenityType)) {
                return "heladeria";
            } else if ("place_of_worship".equalsIgnoreCase(amenityType)) {
                return "Religious Site";
            }
        }

        if (tags.containsKey("historic")) {
            return "Historical Monument";
        }

        if (tags.containsKey("tourism")) {
            return "Tourism Attraction";
        }

        return "Historical Monument";
    }
}
