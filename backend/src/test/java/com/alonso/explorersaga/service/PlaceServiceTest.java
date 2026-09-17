package com.alonso.explorersaga.service;

import com.alonso.explorersaga.dto.place.PlaceCreateRequest;
import com.alonso.explorersaga.dto.place.PlaceResponse;
import com.alonso.explorersaga.entity.Category;
import com.alonso.explorersaga.entity.Place;
import com.alonso.explorersaga.exception.InvalidDataException;
import com.alonso.explorersaga.exception.ResourceNotFoundException;
import com.alonso.explorersaga.repository.CategoryRepository;
import com.alonso.explorersaga.repository.PlaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PlaceServiceTest {

    @Mock
    private PlaceRepository placeRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private PlaceService placeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createPlace_Success() {
        PlaceCreateRequest request = new PlaceCreateRequest();
        request.setName("Teatro");
        request.setCategoryId(1L);
        request.setLatitude(38.0);
        request.setLongitude(-6.0);

        Category category = new Category("MONUMENT", "Desc");
        category.setId(1L);

        Place savedEntity = new Place();
        savedEntity.setId(100L);
        savedEntity.setName("Teatro");
        savedEntity.setCategory(category);
        savedEntity.setLatitude(38.0);
        savedEntity.setLongitude(-6.0);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(placeRepository.save(any(Place.class))).thenReturn(savedEntity);

        PlaceResponse response = placeService.createPlace(request);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getCategory().getName()).isEqualTo("MONUMENT");
    }

    @Test
    void createPlace_InvalidCoordinates_ThrowsInvalidData() {
        PlaceCreateRequest request = new PlaceCreateRequest();
        request.setName("Teatro");
        request.setCategoryId(1L);
        request.setLatitude(100.0); // Invalid
        request.setLongitude(-6.0);

        assertThrows(InvalidDataException.class, () -> placeService.createPlace(request));
    }

    @Test
    void createPlace_CategoryNotFound_ThrowsResourceNotFound() {
        PlaceCreateRequest request = new PlaceCreateRequest();
        request.setName("Teatro");
        request.setCategoryId(1L);
        request.setLatitude(38.0);
        request.setLongitude(-6.0);

        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> placeService.createPlace(request));
    }
}
