package com.alonso.explorersaga.service;

import com.alonso.explorersaga.application.port.in.ImportResult;
import com.alonso.explorersaga.application.port.out.*;
import com.alonso.explorersaga.entity.Category;
import com.alonso.explorersaga.entity.Place;
import com.alonso.explorersaga.entity.PlaceSource;
import com.alonso.explorersaga.entity.Source;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ImportTourismPlacesServiceTest {

    @Mock
    private TourismDataProvider tourismDataProvider;
    @Mock
    private CategoryRepositoryPort categoryRepositoryPort;
    @Mock
    private SourceRepositoryPort sourceRepositoryPort;
    @Mock
    private PlaceRepositoryPort placeRepositoryPort;
    @Mock
    private PlaceSourceRepositoryPort placeSourceRepositoryPort;

    @InjectMocks
    private ImportTourismPlacesService importService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void importPlaces_Success() {
        NormalizedPlaceData data = new NormalizedPlaceData(
                "node/123",
                "OSM_OVERPASS",
                "Teatro Romano de Mérida",
                "Roman theatre",
                "Calle Jose Ramon Melida",
                38.9,
                -6.3,
                "Historical Monument"
        );

        when(tourismDataProvider.fetchTourismPlaces()).thenReturn(List.of(data));
        when(sourceRepositoryPort.findByCode("OSM_OVERPASS")).thenReturn(Optional.empty());
        Source savedSource = new Source("OSM_OVERPASS", "OSM");
        savedSource.setId(1L);
        when(sourceRepositoryPort.save(any(Source.class))).thenReturn(savedSource);

        when(placeSourceRepositoryPort.findBySourceAndExternalId(any(), eq("node/123"))).thenReturn(Optional.empty());

        when(categoryRepositoryPort.findByName("Historical Monument")).thenReturn(Optional.empty());
        Category savedCat = new Category("Historical Monument", "Desc");
        savedCat.setId(1L);
        when(categoryRepositoryPort.save(any(Category.class))).thenReturn(savedCat);

        Place savedPlace = new Place();
        savedPlace.setId(10L);
        when(placeRepositoryPort.save(any(Place.class))).thenReturn(savedPlace);

        ImportResult result = importService.importPlaces();

        assertThat(result.processed()).isEqualTo(1);
        assertThat(result.inserted()).isEqualTo(1);
        assertThat(result.updated()).isEqualTo(0);
        assertThat(result.skipped()).isEqualTo(0);
        assertThat(result.failed()).isEqualTo(0);

        verify(placeRepositoryPort).save(any(Place.class));
        verify(placeSourceRepositoryPort).saveAndFlush(any(PlaceSource.class));
    }

    @Test
    void importPlaces_Idempotent_AlreadyExists_Skipped() {
        NormalizedPlaceData data = new NormalizedPlaceData(
                "node/123",
                "OSM_OVERPASS",
                "Teatro Romano de Mérida",
                "Roman theatre",
                "Calle Jose Ramon Melida",
                38.9,
                -6.3,
                "Historical Monument"
        );

        when(tourismDataProvider.fetchTourismPlaces()).thenReturn(List.of(data));
        Source source = new Source("OSM_OVERPASS", "OSM");
        source.setId(1L);
        when(sourceRepositoryPort.findByCode("OSM_OVERPASS")).thenReturn(Optional.of(source));

        Category category = new Category("Historical Monument", "Desc");
        category.setId(1L);
        when(categoryRepositoryPort.findByName("Historical Monument")).thenReturn(Optional.of(category));

        Place place = new Place();
        place.setName("Teatro Romano de Mérida");
        place.setDescription("Roman theatre");
        place.setAddress("Calle Jose Ramon Melida");
        place.setLatitude(38.9);
        place.setLongitude(-6.3);
        place.setCategory(category);

        PlaceSource placeSource = new PlaceSource();
        placeSource.setPlace(place);
        placeSource.setSource(source);
        placeSource.setExternalId("node/123");

        when(placeSourceRepositoryPort.findBySourceAndExternalId(source, "node/123")).thenReturn(Optional.of(placeSource));

        ImportResult result = importService.importPlaces();

        assertThat(result.processed()).isEqualTo(1);
        assertThat(result.inserted()).isEqualTo(0);
        assertThat(result.updated()).isEqualTo(0);
        assertThat(result.skipped()).isEqualTo(1);
        assertThat(result.failed()).isEqualTo(0);

        verify(placeRepositoryPort, never()).save(any());
    }
}
