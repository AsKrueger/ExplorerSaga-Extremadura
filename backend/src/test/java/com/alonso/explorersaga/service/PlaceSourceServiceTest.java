package com.alonso.explorersaga.service;

import com.alonso.explorersaga.dto.placesource.PlaceSourceCreateRequest;
import com.alonso.explorersaga.dto.placesource.PlaceSourceResponse;
import com.alonso.explorersaga.entity.Place;
import com.alonso.explorersaga.entity.PlaceSource;
import com.alonso.explorersaga.entity.Source;
import com.alonso.explorersaga.exception.DuplicateResourceException;
import com.alonso.explorersaga.exception.ResourceNotFoundException;
import com.alonso.explorersaga.repository.PlaceRepository;
import com.alonso.explorersaga.repository.PlaceSourceRepository;
import com.alonso.explorersaga.repository.SourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PlaceSourceServiceTest {

    @Mock
    private PlaceSourceRepository placeSourceRepository;
    @Mock
    private PlaceRepository placeRepository;
    @Mock
    private SourceRepository sourceRepository;

    @InjectMocks
    private PlaceSourceService placeSourceService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addPlaceSource_Success() {
        PlaceSourceCreateRequest request = new PlaceSourceCreateRequest(1L, 2L, "EXT-ID");
        
        Place place = new Place(); place.setId(1L);
        Source source = new Source("S1", "Name"); source.setId(2L);
        
        PlaceSource saved = new PlaceSource();
        saved.setId(10L);
        saved.setPlace(place);
        saved.setSource(source);
        saved.setExternalId("EXT-ID");

        when(placeRepository.findById(1L)).thenReturn(Optional.of(place));
        when(sourceRepository.findById(2L)).thenReturn(Optional.of(source));
        when(placeSourceRepository.saveAndFlush(any(PlaceSource.class))).thenReturn(saved);

        PlaceSourceResponse response = placeSourceService.addPlaceSource(request);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getExternalId()).isEqualTo("EXT-ID");
    }

    @Test
    void addPlaceSource_DuplicateConstraint_ThrowsDuplicateResource() {
        PlaceSourceCreateRequest request = new PlaceSourceCreateRequest(1L, 2L, "EXT-ID");
        Source source = new Source("S1", "Name"); source.setId(2L);

        when(placeRepository.findById(1L)).thenReturn(Optional.of(new Place()));
        when(sourceRepository.findById(2L)).thenReturn(Optional.of(source));
        when(placeSourceRepository.saveAndFlush(any(PlaceSource.class)))
                .thenThrow(new DataIntegrityViolationException("uk_place_sources_source_external violation"));

        assertThrows(DuplicateResourceException.class, () -> placeSourceService.addPlaceSource(request));
    }

    @Test
    void addPlaceSource_PlaceNotFound_ThrowsResourceNotFound() {
        when(placeRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> placeSourceService.addPlaceSource(new PlaceSourceCreateRequest(1L, 2L, "E")));
    }
}
