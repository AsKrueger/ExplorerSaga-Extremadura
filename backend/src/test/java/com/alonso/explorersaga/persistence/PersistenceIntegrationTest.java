package com.alonso.explorersaga.persistence;

import com.alonso.explorersaga.entity.Category;
import com.alonso.explorersaga.entity.Place;
import com.alonso.explorersaga.entity.PlaceSource;
import com.alonso.explorersaga.entity.Source;
import com.alonso.explorersaga.repository.CategoryRepository;
import com.alonso.explorersaga.repository.PlaceRepository;
import com.alonso.explorersaga.repository.PlaceSourceRepository;
import com.alonso.explorersaga.repository.SourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PersistenceIntegrationTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    private PlaceSourceRepository placeSourceRepository;

    @BeforeEach
    void cleanUp() {
        placeSourceRepository.deleteAll();
        placeRepository.deleteAll();
        sourceRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    void shouldPersistMultiSourcePlace() {
        // 1. Create Category
        Category category = new Category("MONUMENT", "Historical monuments");
        category = categoryRepository.save(category);

        // 2. Create Place
        Place place = new Place();
        place.setName("Teatro Romano");
        place.setDescription("Ancient Roman theatre");
        place.setAddress("Plaza Margarita Xirgú, s/n");
        place.setLatitude(38.9153);
        place.setLongitude(-6.3386);
        place.setCategory(category);
        place = placeRepository.save(place);

        // 3. Create Sources
        Source google = new Source("GOOGLE_PLACES", "Google Places API");
        google = sourceRepository.save(google);

        Source openData = new Source("OPEN_DATA_EXTREMADURA", "Open Data Extremadura");
        openData = sourceRepository.save(openData);

        // 4. Create PlaceSources
        PlaceSource ps1 = new PlaceSource();
        ps1.setPlace(place);
        ps1.setSource(google);
        ps1.setExternalId("ChIJxxxxx");
        ps1.setLastSync(OffsetDateTime.now());
        placeSourceRepository.save(ps1);

        PlaceSource ps2 = new PlaceSource();
        ps2.setPlace(place);
        ps2.setSource(openData);
        ps2.setExternalId("BIC-12345");
        ps2.setLastSync(OffsetDateTime.now());
        placeSourceRepository.save(ps2);

        // 5. Verify
        Place savedPlace = placeRepository.findById(place.getId()).orElseThrow();
        assertThat(savedPlace.getName()).isEqualTo("Teatro Romano");
        
        List<PlaceSource> sources = placeSourceRepository.findAll();
        assertThat(sources).hasSize(2);
        assertThat(sources).extracting(ps -> ps.getSource().getCode())
                .containsExactlyInAnyOrder("GOOGLE_PLACES", "OPEN_DATA_EXTREMADURA");
    }

    @Test
    void shouldViolateUniqueConstraintOnPlaceSource() {
        Category cat = categoryRepository.save(new Category("CAT1", "desc"));
        Source source = sourceRepository.save(new Source("S1", "name"));
        
        Place p1 = new Place();
        p1.setName("P1");
        p1.setLatitude(0.0);
        p1.setLongitude(0.0);
        p1.setCategory(cat);
        p1 = placeRepository.save(p1);

        Place p2 = new Place();
        p2.setName("P2");
        p2.setLatitude(1.0);
        p2.setLongitude(1.0);
        p2.setCategory(cat);
        p2 = placeRepository.save(p2);

        PlaceSource ps1 = new PlaceSource();
        ps1.setPlace(p1);
        ps1.setSource(source);
        ps1.setExternalId("EXT-ID");
        placeSourceRepository.save(ps1);

        PlaceSource ps2 = new PlaceSource();
        ps2.setPlace(p2);
        ps2.setSource(source);
        ps2.setExternalId("EXT-ID"); // Duplicate external ID for same source

        PlaceSource finalPs2 = ps2;
        assertThrows(DataIntegrityViolationException.class, () -> {
            placeSourceRepository.saveAndFlush(finalPs2);
        });
    }

    @Test
    void shouldPreventDeletingCategoryWithPlaces() {
        Category category = new Category("PROTECTED", "desc");
        category = categoryRepository.save(category);

        Place place = new Place();
        place.setName("Protected Place");
        place.setLatitude(0.0);
        place.setLongitude(0.0);
        place.setCategory(category);
        placeRepository.save(place);

        Category finalCategory = category;
        assertThrows(DataIntegrityViolationException.class, () -> {
            categoryRepository.delete(finalCategory);
            categoryRepository.flush();
        });
    }
}
