package com.alonso.explorersaga.service;

import com.alonso.explorersaga.dto.placesource.PlaceSourceCreateRequest;
import com.alonso.explorersaga.dto.placesource.PlaceSourceResponse;
import com.alonso.explorersaga.dto.source.SourceResponse;
import com.alonso.explorersaga.entity.Place;
import com.alonso.explorersaga.entity.PlaceSource;
import com.alonso.explorersaga.entity.Source;
import com.alonso.explorersaga.exception.DuplicateResourceException;
import com.alonso.explorersaga.exception.InvalidDataException;
import com.alonso.explorersaga.exception.ResourceNotFoundException;
import com.alonso.explorersaga.repository.PlaceRepository;
import com.alonso.explorersaga.repository.PlaceSourceRepository;
import com.alonso.explorersaga.repository.SourceRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class PlaceSourceService {

    private final PlaceSourceRepository placeSourceRepository;
    private final PlaceRepository placeRepository;
    private final SourceRepository sourceRepository;

    public PlaceSourceService(PlaceSourceRepository placeSourceRepository,
                              PlaceRepository placeRepository,
                              SourceRepository sourceRepository) {
        this.placeSourceRepository = placeSourceRepository;
        this.placeRepository = placeRepository;
        this.sourceRepository = sourceRepository;
    }

    @Transactional
    public PlaceSourceResponse addPlaceSource(PlaceSourceCreateRequest request) {
        validateRequest(request);

        Place place = placeRepository.findById(request.getPlaceId())
                .orElseThrow(() -> new ResourceNotFoundException("Place with ID " + request.getPlaceId() + " not found"));

        Source source = sourceRepository.findById(request.getSourceId())
                .orElseThrow(() -> new ResourceNotFoundException("Source with ID " + request.getSourceId() + " not found"));

        PlaceSource placeSource = new PlaceSource();
        placeSource.setPlace(place);
        placeSource.setSource(source);
        placeSource.setExternalId(request.getExternalId());
        placeSource.setLastSync(OffsetDateTime.now());

        try {
            PlaceSource saved = placeSourceRepository.saveAndFlush(placeSource);
            return mapToResponse(saved);
        } catch (DataIntegrityViolationException e) {
            // Check if it's the unique constraint violation
            if (e.getMessage() != null && e.getMessage().contains("uk_place_sources_source_external")) {
                throw new DuplicateResourceException("PlaceSource with external ID '" + request.getExternalId() + 
                    "' already exists for source '" + source.getCode() + "'");
            }
            throw e; // Re-throw other integrity violations
        }
    }

    private void validateRequest(PlaceSourceCreateRequest request) {
        if (request.getPlaceId() == null) {
            throw new InvalidDataException("Place ID is required");
        }
        if (request.getSourceId() == null) {
            throw new InvalidDataException("Source ID is required");
        }
        if (request.getExternalId() == null || request.getExternalId().trim().isEmpty()) {
            throw new InvalidDataException("External ID is required");
        }
    }

    private PlaceSourceResponse mapToResponse(PlaceSource entity) {
        PlaceSourceResponse response = new PlaceSourceResponse();
        response.setId(entity.getId());
        response.setPlaceId(entity.getPlace().getId());
        response.setExternalId(entity.getExternalId());
        response.setLastSync(entity.getLastSync());
        
        Source source = entity.getSource();
        response.setSource(new SourceResponse(source.getId(), source.getCode(), source.getName(), source.getDescription()));
        
        return response;
    }
}
