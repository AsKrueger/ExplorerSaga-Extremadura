package com.alonso.explorersaga.service;

import com.alonso.explorersaga.dto.category.CategoryResponse;
import com.alonso.explorersaga.dto.place.PlaceCreateRequest;
import com.alonso.explorersaga.dto.place.PlaceResponse;
import com.alonso.explorersaga.entity.Category;
import com.alonso.explorersaga.entity.Place;
import com.alonso.explorersaga.exception.InvalidDataException;
import com.alonso.explorersaga.exception.ResourceNotFoundException;
import com.alonso.explorersaga.repository.CategoryRepository;
import com.alonso.explorersaga.repository.PlaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlaceService {

    private final PlaceRepository placeRepository;
    private final CategoryRepository categoryRepository;

    public PlaceService(PlaceRepository placeRepository, CategoryRepository categoryRepository) {
        this.placeRepository = placeRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public PlaceResponse createPlace(PlaceCreateRequest request) {
        validateRequest(request);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category with ID " + request.getCategoryId() + " not found"));

        Place place = new Place();
        place.setName(request.getName());
        place.setDescription(request.getDescription());
        place.setAddress(request.getAddress());
        place.setLatitude(request.getLatitude());
        place.setLongitude(request.getLongitude());
        place.setCategory(category);

        Place saved = placeRepository.save(place);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public PlaceResponse getPlace(Long id) {
        return placeRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Place with ID " + id + " not found"));
    }

    @Transactional(readOnly = true)
    public List<PlaceResponse> listPlaces() {
        return placeRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private void validateRequest(PlaceCreateRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new InvalidDataException("Place name is required");
        }
        if (request.getCategoryId() == null) {
            throw new InvalidDataException("Category ID is required");
        }
        if (request.getLatitude() == null || request.getLatitude() < -90 || request.getLatitude() > 90) {
            throw new InvalidDataException("Latitude must be between -90 and 90");
        }
        if (request.getLongitude() == null || request.getLongitude() < -180 || request.getLongitude() > 180) {
            throw new InvalidDataException("Longitude must be between -180 and 180");
        }
    }

    private PlaceResponse mapToResponse(Place entity) {
        PlaceResponse response = new PlaceResponse();
        response.setId(entity.getId());
        response.setName(entity.getName());
        response.setDescription(entity.getDescription());
        response.setAddress(entity.getAddress());
        response.setLatitude(entity.getLatitude());
        response.setLongitude(entity.getLongitude());
        
        Category category = entity.getCategory();
        CategoryResponse categoryResponse = new CategoryResponse(category.getId(), category.getName(), category.getDescription());
        response.setCategory(categoryResponse);
        
        return response;
    }
}
