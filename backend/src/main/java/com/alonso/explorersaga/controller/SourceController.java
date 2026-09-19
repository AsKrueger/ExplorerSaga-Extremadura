package com.alonso.explorersaga.controller;

import com.alonso.explorersaga.dto.source.SourceCreateRequest;
import com.alonso.explorersaga.dto.source.SourceResponse;
import com.alonso.explorersaga.service.SourceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sources")
public class SourceController {

    private final SourceService sourceService;

    public SourceController(SourceService sourceService) {
        this.sourceService = sourceService;
    }

    @PostMapping
    public ResponseEntity<SourceResponse> createSource(@Valid @RequestBody SourceCreateRequest request) {
        SourceResponse response = sourceService.createSource(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SourceResponse> getSource(@PathVariable Long id) {
        return ResponseEntity.ok(sourceService.getSource(id));
    }

    @GetMapping
    public ResponseEntity<List<SourceResponse>> listSources() {
        return ResponseEntity.ok(sourceService.listSources());
    }
}
