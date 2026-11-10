package com.alonso.explorersaga.adapters.out.external;

import com.alonso.explorersaga.application.port.out.NormalizedPlaceData;
import com.alonso.explorersaga.application.port.out.TourismDataProvider;
import com.alonso.explorersaga.exception.ExternalProviderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class OverpassAdapter implements TourismDataProvider {

    private static final Logger logger = LoggerFactory.getLogger(OverpassAdapter.class);

    private static final String QUERY = """
            [out:json][timeout:25];
            (
              node["tourism"](38.88,-6.36,38.95,-6.30);
              way["tourism"](38.88,-6.36,38.95,-6.30);
              node["historic"](38.88,-6.36,38.95,-6.30);
              way["historic"](38.88,-6.36,38.95,-6.30);
              node["amenity"~"place_of_worship|restaurant|cafe|bar|pub|ice_cream"](38.88,-6.36,38.95,-6.30);
              node["shop"](38.88,-6.36,38.95,-6.30);
            );
            out center;
            """;

    // Valores de "tourism" que no son lugares de visita (alojamientos y puntos de servicio)
    private static final Set<String> NON_VISITOR_TOURISM = Set.of(
            "hotel", "hostel", "guest_house", "apartment", "motel", "chalet",
            "camp_site", "caravan_site", "information", "alpine_hut", "wilderness_hut"
    );

    // Comercios de servicios o vehículos sin interés para una guía turística
    private static final Set<String> NON_VISITOR_SHOPS = Set.of(
            "car", "car_repair", "car_parts", "motorcycle", "tyres", "fuel",
            "hairdresser", "beauty", "laundry", "dry_cleaning", "bookmaker", "lottery",
            "funeral_directors", "doityourself", "hardware", "trade", "storage_rental",
            "money_lender", "pawnbroker"
    );

    private static final Set<String> FOOD_SHOPS = Set.of(
            "food", "bakery", "deli", "greengrocer", "butcher", "pastry",
            "confectionery", "tea", "coffee", "wine", "cheese", "farm"
    );

    private final RestTemplate restTemplate;
    private final List<String> baseUrls;
    private final int maxAttempts;
    private final long retryBackoffMs;

    @Autowired
    public OverpassAdapter(
            RestTemplate restTemplate,
            @Value("${explorersaga.providers.overpass.base-urls:https://overpass-api.de/api/interpreter,https://overpass.kumi.systems/api/interpreter}") List<String> baseUrls,
            @Value("${explorersaga.providers.overpass.max-attempts:2}") int maxAttempts,
            @Value("${explorersaga.providers.overpass.retry-backoff-ms:2000}") long retryBackoffMs) {
        this.restTemplate = restTemplate;
        this.baseUrls = List.copyOf(baseUrls);
        this.maxAttempts = Math.max(1, maxAttempts);
        this.retryBackoffMs = Math.max(0, retryBackoffMs);
    }

    public OverpassAdapter(RestTemplate restTemplate, String baseUrl) {
        this(restTemplate, List.of(baseUrl), 1, 0);
    }

    @Override
    public List<NormalizedPlaceData> fetchTourismPlaces() {
        OverpassResponseDto response = fetchWithRetries();
        if (response.getElements() == null) {
            return Collections.emptyList();
        }

        List<NormalizedPlaceData> normalizedList = new ArrayList<>();
        for (OverpassElementDto element : response.getElements()) {
            Map<String, String> tags = element.getTags();
            if (tags == null || !tags.containsKey("name")) {
                continue;
            }
            Double lat = element.getLat();
            Double lon = element.getLon();
            if (lat == null || lon == null) {
                continue;
            }
            String categoryName = mapTagsToCategory(tags);
            if (categoryName == null) {
                continue;
            }

            String address = tags.get("addr:street");
            if (address == null) {
                address = tags.get("address");
            }

            normalizedList.add(new NormalizedPlaceData(
                    element.getType() + "/" + element.getId(),
                    "OSM_OVERPASS",
                    tags.get("name"),
                    tags.get("description"),
                    address,
                    lat,
                    lon,
                    categoryName
            ));
        }
        return normalizedList;
    }

    // Recorre las instancias configuradas en rondas, con espera exponencial entre rondas.
    // La instancia pública principal rechaza a menudo peticiones por saturación (HTTP 502/504)
    private OverpassResponseDto fetchWithRetries() {
        RestClientException lastError = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            if (attempt > 1) {
                sleep(retryBackoffMs * (1L << (attempt - 2)));
            }
            for (String baseUrl : baseUrls) {
                // Se pasa un URI ya codificado para que RestTemplate no vuelva a codificar la consulta
                URI requestUri = URI.create(baseUrl + "?data=" + URLEncoder.encode(QUERY, StandardCharsets.UTF_8));
                try {
                    OverpassResponseDto response = restTemplate.getForObject(requestUri, OverpassResponseDto.class);
                    if (response != null) {
                        return response;
                    }
                } catch (RestClientException e) {
                    logger.warn("Overpass request to {} failed (attempt {}/{}): {}", baseUrl, attempt, maxAttempts, e.getMessage());
                    lastError = e;
                }
            }
        }
        String detail = lastError != null ? lastError.getMessage() : "respuesta vacía";
        throw new ExternalProviderException(
                "Overpass API no está disponible tras " + maxAttempts + " ronda(s) de intentos: " + detail, lastError);
    }

    private void sleep(long millis) {
        if (millis <= 0) {
            return;
        }
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExternalProviderException("Importación interrumpida durante la espera entre reintentos", e);
        }
    }

    // Normaliza las etiquetas de OSM al vocabulario de categorías en español del proyecto
    // (seed de Flyway y datos locales de Android). Devuelve null para descartar el elemento
    private String mapTagsToCategory(Map<String, String> tags) {
        String shop = tags.get("shop");
        if (shop != null) {
            if (NON_VISITOR_SHOPS.contains(shop)) return null;
            if ("books".equals(shop)) return "libreria";
            if ("gift".equals(shop) || "souvenir".equals(shop)) return "souvenir";
            if ("supermarket".equals(shop) || "convenience".equals(shop)) return "supermercado";
            if (FOOD_SHOPS.contains(shop)) return "tienda_alimentos";
            if ("clothes".equals(shop) || "fashion".equals(shop) || "shoes".equals(shop)) return "ropa";
            return "tienda_general";
        }

        String amenity = tags.get("amenity");
        if (amenity != null) {
            switch (amenity) {
                case "restaurant": return "restaurante";
                case "cafe": return "cafeteria";
                case "bar":
                case "pub": return "bar";
                case "ice_cream": return "heladeria";
                case "place_of_worship": return "iglesia";
                default: break;
            }
        }

        String tourism = tags.get("tourism");
        if ("museum".equals(tourism) || "gallery".equals(tourism)) return "museo";
        if (tags.containsKey("historic")) return "monumento";
        if (tourism != null && !NON_VISITOR_TOURISM.contains(tourism)) return "monumento";
        return null;
    }
}
