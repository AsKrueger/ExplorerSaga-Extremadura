package com.alonso.explorersaga.service;

import com.alonso.explorersaga.application.port.in.ImportResult;
import com.alonso.explorersaga.application.port.in.ImportTourismPlacesUseCase;
import com.alonso.explorersaga.application.port.out.*;
import com.alonso.explorersaga.entity.Category;
import com.alonso.explorersaga.entity.Place;
import com.alonso.explorersaga.entity.PlaceSource;
import com.alonso.explorersaga.entity.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
public class ImportTourismPlacesService implements ImportTourismPlacesUseCase {

    private static final Logger logger = LoggerFactory.getLogger(ImportTourismPlacesService.class);

    // Radio para considerar que dos registros con el mismo nombre son el mismo lugar.
    // Los monumentos pueden ser extensos (el acueducto se compone de varios tramos en OSM);
    // en comercios se usa un radio corto para no fusionar locales distintos de una misma cadena
    private static final double MONUMENT_MATCH_RADIUS_METERS = 750;
    private static final double DEFAULT_MATCH_RADIUS_METERS = 100;
    private static final Set<String> EXTENSIVE_CATEGORIES = Set.of("monumento", "museo", "iglesia");
    private static final double EARTH_RADIUS_METERS = 6_371_008.8;

    private final TourismDataProvider tourismDataProvider;
    private final CategoryRepositoryPort categoryRepositoryPort;
    private final SourceRepositoryPort sourceRepositoryPort;
    private final PlaceRepositoryPort placeRepositoryPort;
    private final PlaceSourceRepositoryPort placeSourceRepositoryPort;

    public ImportTourismPlacesService(
            TourismDataProvider tourismDataProvider,
            CategoryRepositoryPort categoryRepositoryPort,
            SourceRepositoryPort sourceRepositoryPort,
            PlaceRepositoryPort placeRepositoryPort,
            PlaceSourceRepositoryPort placeSourceRepositoryPort) {
        this.tourismDataProvider = tourismDataProvider;
        this.categoryRepositoryPort = categoryRepositoryPort;
        this.sourceRepositoryPort = sourceRepositoryPort;
        this.placeRepositoryPort = placeRepositoryPort;
        this.placeSourceRepositoryPort = placeSourceRepositoryPort;
    }

    @Override
    @Transactional
    public ImportResult importPlaces() {
        List<NormalizedPlaceData> rawPlaces = tourismDataProvider.fetchTourismPlaces();
        // Lugares ya registrados (seed e importaciones previas) para detectar duplicados por nombre y cercanía
        List<Place> knownPlaces = new ArrayList<>(placeRepositoryPort.findAll());
        int processed = 0;
        int inserted = 0;
        int updated = 0;
        int skipped = 0;
        int failed = 0;
        int merged = 0;

        for (NormalizedPlaceData data : rawPlaces) {
            processed++;
            try {
                if (data.name() == null || data.name().isBlank() || data.latitude() == null || data.longitude() == null) {
                    skipped++;
                    continue;
                }

                // 1. Find or create Source
                Source source = sourceRepositoryPort.findByCode(data.sourceCode())
                        .orElseGet(() -> {
                            Source newSource = new Source(data.sourceCode(), "OpenStreetMap Overpass API");
                            newSource.setDescription("External OSM provider");
                            return sourceRepositoryPort.save(newSource);
                        });

                // 2. Check if PlaceSource already exists
                Optional<PlaceSource> existing = placeSourceRepositoryPort.findBySourceAndExternalId(source, data.externalId());

                // 3. Find or create Category
                String catName = data.categoryName() != null ? data.categoryName() : "monumento";
                Category category = categoryRepositoryPort.findByName(catName)
                        .orElseGet(() -> {
                            Category newCat = new Category(catName, "Imported category");
                            return categoryRepositoryPort.save(newCat);
                        });

                if (existing.isPresent()) {
                    Place place = existing.get().getPlace();
                    // Un lugar enlazado a varias fuentes (seed + OSM, o varios elementos OSM) conserva sus
                    // datos: si cada fuente los sobrescribiera, la importación dejaría de ser idempotente
                    if (placeSourceRepositoryPort.countByPlace(place) > 1) {
                        skipped++;
                        continue;
                    }
                    boolean changed = false;
                    if (data.name() != null && !data.name().equals(place.getName())) {
                        place.setName(data.name());
                        changed = true;
                    }
                    if (data.description() != null && !data.description().equals(place.getDescription())) {
                        place.setDescription(data.description());
                        changed = true;
                    }
                    if (data.address() != null && !data.address().equals(place.getAddress())) {
                        place.setAddress(data.address());
                        changed = true;
                    }
                    if (data.latitude() != null && !data.latitude().equals(place.getLatitude())) {
                        place.setLatitude(data.latitude());
                        changed = true;
                    }
                    if (data.longitude() != null && !data.longitude().equals(place.getLongitude())) {
                        place.setLongitude(data.longitude());
                        changed = true;
                    }
                    if (!place.getCategory().equals(category)) {
                        place.setCategory(category);
                        changed = true;
                    }

                    if (changed) {
                        placeRepositoryPort.save(place);
                        existing.get().setLastSync(OffsetDateTime.now());
                        placeSourceRepositoryPort.saveAndFlush(existing.get());
                        updated++;
                    } else {
                        skipped++;
                    }
                } else {
                    Optional<Place> duplicate = findDuplicate(knownPlaces, data);
                    Place targetPlace;
                    if (duplicate.isPresent()) {
                        // Mismo lugar ya registrado: solo se enlaza la nueva fuente externa
                        targetPlace = duplicate.get();
                        merged++;
                    } else {
                        Place place = new Place();
                        place.setName(data.name());
                        place.setDescription(data.description());
                        place.setAddress(data.address());
                        place.setLatitude(data.latitude());
                        place.setLongitude(data.longitude());
                        place.setCategory(category);
                        targetPlace = placeRepositoryPort.save(place);
                        knownPlaces.add(targetPlace);
                        inserted++;
                    }

                    PlaceSource placeSource = new PlaceSource();
                    placeSource.setPlace(targetPlace);
                    placeSource.setSource(source);
                    placeSource.setExternalId(data.externalId());
                    placeSource.setLastSync(OffsetDateTime.now());
                    placeSourceRepositoryPort.saveAndFlush(placeSource);
                }
            } catch (Exception e) {
                logger.error("Failed to import place {}: {}", data.externalId(), e.getMessage());
                failed++;
            }
        }

        return new ImportResult(processed, inserted, updated, skipped, failed, merged);
    }

    private Optional<Place> findDuplicate(List<Place> knownPlaces, NormalizedPlaceData data) {
        String name = normalizeName(data.name());
        double radius = EXTENSIVE_CATEGORIES.contains(data.categoryName())
                ? MONUMENT_MATCH_RADIUS_METERS
                : DEFAULT_MATCH_RADIUS_METERS;
        return knownPlaces.stream()
                .filter(place -> normalizeName(place.getName()).equals(name))
                .filter(place -> distanceInMeters(place.getLatitude(), place.getLongitude(),
                        data.latitude(), data.longitude()) <= radius)
                .findFirst();
    }

    // Minúsculas, sin tildes y con espacios simples, para comparar "Templo de Diana" con "templo  de diána"
    static String normalizeName(String name) {
        String withoutAccents = Normalizer.normalize(name, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return withoutAccents.toLowerCase(Locale.ROOT).trim().replaceAll("\\s+", " ");
    }

    static double distanceInMeters(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return EARTH_RADIUS_METERS * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
