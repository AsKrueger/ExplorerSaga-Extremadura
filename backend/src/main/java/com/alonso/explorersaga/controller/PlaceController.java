package com.alonso.explorersaga.controller;

import com.alonso.explorersaga.dto.place.PlaceCreateRequest;
import com.alonso.explorersaga.dto.place.PlaceResponse;
import com.alonso.explorersaga.service.PlaceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/places")
public class PlaceController {

    private final PlaceService placeService;

    public PlaceController(PlaceService placeService) {
        this.placeService = placeService;
    }

    @PostMapping
    public ResponseEntity<PlaceResponse> createPlace(@Valid @RequestBody PlaceCreateRequest request) {
        PlaceResponse response = placeService.createPlace(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlaceResponse> getPlace(@PathVariable Long id) {
        return ResponseEntity.ok(placeService.getPlace(id));
    }

    @GetMapping
    public ResponseEntity<List<PlaceResponse>> listPlaces() {
        return ResponseEntity.ok(placeService.listPlaces());
    }
}
