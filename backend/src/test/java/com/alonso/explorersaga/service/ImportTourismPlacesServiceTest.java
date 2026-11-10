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
import static org.mockito.ArgumentMatchers.argThat;
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

    @Test
    void importPlaces_SameNameNearbyMonument_MergedIntoExistingPlace() {
        Source source = stubOsmSource();
        Category monumento = stubCategory("monumento");
        Place seedPlace = place("Acueducto de los Milagros", 38.923889, -6.348611, monumento);
        when(placeRepositoryPort.findAll()).thenReturn(List.of(seedPlace));
        // Tramo de OSM a unos 350 m del registro del seed, con tildes y mayúsculas distintas
        NormalizedPlaceData segment = data("way/202213492", "ACUEDUCTO DE LOS MILÁGROS", 38.92696, -6.34777, "monumento");
        when(tourismDataProvider.fetchTourismPlaces()).thenReturn(List.of(segment));

        ImportResult result = importService.importPlaces();

        assertThat(result.merged()).isEqualTo(1);
        assertThat(result.inserted()).isEqualTo(0);
        verify(placeRepositoryPort, never()).save(any());
        verify(placeSourceRepositoryPort).saveAndFlush(argThat(ps ->
                ps.getPlace() == seedPlace && ps.getSource() == source && ps.getExternalId().equals("way/202213492")));
    }

    @Test
    void importPlaces_SameNameDistantShop_InsertedAsDifferentPlace() {
        stubOsmSource();
        Category supermercado = stubCategory("supermercado");
        Place existingStore = place("Carrefour Express", 38.9165, -6.3478, supermercado);
        when(placeRepositoryPort.findAll()).thenReturn(List.of(existingStore));
        when(placeRepositoryPort.save(any(Place.class))).thenAnswer(invocation -> invocation.getArgument(0));
        // Otro local de la misma cadena a unos 400 m: no debe fusionarse
        NormalizedPlaceData otherStore = data("node/55", "Carrefour Express", 38.9200, -6.3460, "supermercado");
        when(tourismDataProvider.fetchTourismPlaces()).thenReturn(List.of(otherStore));

        ImportResult result = importService.importPlaces();

        assertThat(result.inserted()).isEqualTo(1);
        assertThat(result.merged()).isEqualTo(0);
    }

    @Test
    void importPlaces_PlaceWithSeveralSources_IsNotOverwritten() {
        Source source = stubOsmSource();
        Category monumento = stubCategory("monumento");
        Place seedPlace = place("Templo de Diana", 38.917, -6.344, monumento);
        PlaceSource osmLink = new PlaceSource();
        osmLink.setPlace(seedPlace);
        osmLink.setSource(source);
        osmLink.setExternalId("way/1");
        when(placeSourceRepositoryPort.findBySourceAndExternalId(source, "way/1")).thenReturn(Optional.of(osmLink));
        when(placeSourceRepositoryPort.countByPlace(seedPlace)).thenReturn(2L);
        NormalizedPlaceData moved = data("way/1", "Templo de Diana", 38.9175, -6.3445, "monumento");
        when(tourismDataProvider.fetchTourismPlaces()).thenReturn(List.of(moved));

        ImportResult result = importService.importPlaces();

        assertThat(result.skipped()).isEqualTo(1);
        assertThat(result.updated()).isEqualTo(0);
        assertThat(seedPlace.getLatitude()).isEqualTo(38.917);
        verify(placeRepositoryPort, never()).save(any());
    }

    @Test
    void normalizeName_ignoresAccentsCaseAndExtraSpaces() {
        assertThat(ImportTourismPlacesService.normalizeName("  Templo   de DIÁNA "))
                .isEqualTo(ImportTourismPlacesService.normalizeName("templo de diana"));
    }

    private Source stubOsmSource() {
        Source source = new Source("OSM_OVERPASS", "OSM");
        source.setId(1L);
        when(sourceRepositoryPort.findByCode("OSM_OVERPASS")).thenReturn(Optional.of(source));
        return source;
    }

    private Category stubCategory(String name) {
        Category category = new Category(name, "Desc");
        category.setId(1L);
        when(categoryRepositoryPort.findByName(name)).thenReturn(Optional.of(category));
        return category;
    }

    private static Place place(String name, double lat, double lon, Category category) {
        Place place = new Place();
        place.setName(name);
        place.setLatitude(lat);
        place.setLongitude(lon);
        place.setCategory(category);
        return place;
    }

    private static NormalizedPlaceData data(String externalId, String name, double lat, double lon, String category) {
        return new NormalizedPlaceData(externalId, "OSM_OVERPASS", name, null, null, lat, lon, category);
    }
}
