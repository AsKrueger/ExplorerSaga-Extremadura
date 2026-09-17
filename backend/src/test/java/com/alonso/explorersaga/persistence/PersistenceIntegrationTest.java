package com.alonso.explorersaga.persistence;

import com.alonso.explorersaga.entity.Category;
import com.alonso.explorersaga.entity.Place;
import com.alonso.explorersaga.entity.PlaceSource;
import com.alonso.explorersaga.entity.Source;
import com.alonso.explorersaga.repository.CategoryRepository;
import com.alonso.explorersaga.repository.PlaceRepository;
import com.alonso.explorersaga.repository.PlaceSourceRepository;
import com.alonso.explorersaga.repository.SourceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@ActiveProfiles("test")
class PersistenceIntegrationTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    private PlaceSourceRepository placeSourceRepository;

    @Test
    void shouldPersistMultiSourcePlace() {
        // 1. Create Category
        Category category = new Category("MONUMENT", "Historical monuments");
        category = categoryRepository.save(category);

        // 2. Create Place
        Place place = new Place();
        place.setName("Teatro Romano");
        place.setDescription("Ancient Roman theatre");
        place.setCategory(category);
        place = placeRepository.save(place);

        // 3. Create Sources
        Source turismoMerida = new Source("TURISMO_MERIDA", "https://turismomerida.org");
        turismoMerida = sourceRepository.save(turismoMerida);

        Source googlePlaces = new Source("GOOGLE_PLACES", "https://maps.googleapis.com");
        googlePlaces = sourceRepository.save(googlePlaces);

        // 4. Create PlaceSources
        PlaceSource ps1 = new PlaceSource();
        ps1.setPlace(place);
        ps1.setSource(turismoMerida);
        ps1.setExternalId("BIC-123");
        ps1.setLastSync(LocalDateTime.now());
        placeSourceRepository.save(ps1);

        PlaceSource ps2 = new PlaceSource();
        ps2.setPlace(place);
        ps2.setSource(googlePlaces);
        ps2.setExternalId("ChIJ-google-id");
        ps2.setLastSync(LocalDateTime.now());
        placeSourceRepository.save(ps2);

        // 5. Verify
        Place savedPlace = placeRepository.findById(place.getId()).orElseThrow();
        assertThat(savedPlace.getName()).isEqualTo("Teatro Romano");
        assertThat(savedPlace.getCategory().getName()).isEqualTo("MONUMENT");

        List<PlaceSource> sources = placeSourceRepository.findAll();
        assertThat(sources).hasSize(2);
        assertThat(sources).extracting(ps -> ps.getSource().getName())
                .containsExactlyInAnyOrder("TURISMO_MERIDA", "GOOGLE_PLACES");
    }

    @Test
    void shouldViolateUniqueConstraintOnPlaceSource() {
        Source source = sourceRepository.save(new Source("SOURCE1", "url"));
        
        Place place1 = new Place();
        place1.setName("Place 1");
        place1 = placeRepository.save(place1);

        Place place2 = new Place();
        place2.setName("Place 2");
        place2 = placeRepository.save(place2);

        PlaceSource ps1 = new PlaceSource();
        ps1.setPlace(place1);
        ps1.setSource(source);
        ps1.setExternalId("EXT-001");
        placeSourceRepository.save(ps1);

        PlaceSource ps2 = new PlaceSource();
        ps2.setPlace(place2);
        ps2.setSource(source);
        ps2.setExternalId("EXT-001"); // Duplicate external ID for same source

        PlaceSource ps2Final = ps2;
        assertThrows(DataIntegrityViolationException.class, () -> {
            placeSourceRepository.saveAndFlush(ps2Final);
        });
    }
}
