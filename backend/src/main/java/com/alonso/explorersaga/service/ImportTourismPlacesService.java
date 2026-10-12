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

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ImportTourismPlacesService implements ImportTourismPlacesUseCase {

    private static final Logger logger = LoggerFactory.getLogger(ImportTourismPlacesService.class);

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
        int processed = 0;
        int inserted = 0;
        int skipped = 0;
        int failed = 0;

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
                if (existing.isPresent()) {
                    skipped++;
                    continue;
                }

                // 3. Find or create Category
                String catName = data.categoryName() != null ? data.categoryName() : "Monument";
                Category category = categoryRepositoryPort.findByName(catName)
                        .orElseGet(() -> {
                            Category newCat = new Category(catName, "Imported category");
                            return categoryRepositoryPort.save(newCat);
                        });

                // 4. Save Place
                Place place = new Place();
                place.setName(data.name());
                place.setDescription(data.description());
                place.setAddress(data.address());
                place.setLatitude(data.latitude());
                place.setLongitude(data.longitude());
                place.setCategory(category);
                Place savedPlace = placeRepositoryPort.save(place);

                // 5. Save PlaceSource
                PlaceSource placeSource = new PlaceSource();
                placeSource.setPlace(savedPlace);
                placeSource.setSource(source);
                placeSource.setExternalId(data.externalId());
                placeSource.setLastSync(OffsetDateTime.now());
                placeSourceRepositoryPort.saveAndFlush(placeSource);

                inserted++;
            } catch (Exception e) {
                logger.error("Failed to import place {}: {}", data.externalId(), e.getMessage());
                failed++;
            }
        }

        return new ImportResult(processed, inserted, skipped, failed);
    }
}
