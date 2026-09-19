package com.alonso.explorersaga.controller;

import com.alonso.explorersaga.dto.placesource.PlaceSourceCreateRequest;
import com.alonso.explorersaga.dto.placesource.PlaceSourceResponse;
import com.alonso.explorersaga.service.PlaceSourceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/place-sources")
public class PlaceSourceController {

    private final PlaceSourceService placeSourceService;

    public PlaceSourceController(PlaceSourceService placeSourceService) {
        this.placeSourceService = placeSourceService;
    }

    @PostMapping
    public ResponseEntity<PlaceSourceResponse> addPlaceSource(@Valid @RequestBody PlaceSourceCreateRequest request) {
        PlaceSourceResponse response = placeSourceService.addPlaceSource(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
