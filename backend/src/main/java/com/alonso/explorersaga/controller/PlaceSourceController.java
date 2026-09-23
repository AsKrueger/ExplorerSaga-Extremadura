package com.alonso.explorersaga.controller;

import com.alonso.explorersaga.dto.common.ErrorResponse;
import com.alonso.explorersaga.dto.placesource.PlaceSourceCreateRequest;
import com.alonso.explorersaga.dto.placesource.PlaceSourceResponse;
import com.alonso.explorersaga.service.PlaceSourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/place-sources")
@Tag(name = "Place Sources", description = "Asociación entre lugares e identificadores de fuentes externas")
public class PlaceSourceController {

    private final PlaceSourceService placeSourceService;

    public PlaceSourceController(PlaceSourceService placeSourceService) {
        this.placeSourceService = placeSourceService;
    }

    @PostMapping
    @Operation(summary = "Vincular un lugar con una fuente externa", description = "Registra la relación única (sourceId, externalId) asociando un lugar interno con el identificador del proveedor externo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Vínculo creado con éxito",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PlaceSourceResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o faltantes",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Lugar o fuente especificados no encontrados",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflicto: la combinación de fuente y ID externo ya está registrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PlaceSourceResponse> addPlaceSource(@Valid @RequestBody PlaceSourceCreateRequest request) {
        PlaceSourceResponse response = placeSourceService.addPlaceSource(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
