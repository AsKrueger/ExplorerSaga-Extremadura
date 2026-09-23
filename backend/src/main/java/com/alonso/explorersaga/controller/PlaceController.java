package com.alonso.explorersaga.controller;

import com.alonso.explorersaga.dto.common.ErrorResponse;
import com.alonso.explorersaga.dto.place.PlaceCreateRequest;
import com.alonso.explorersaga.dto.place.PlaceResponse;
import com.alonso.explorersaga.service.PlaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/places")
@Tag(name = "Places", description = "Gestión de lugares turísticos")
public class PlaceController {

    private final PlaceService placeService;

    public PlaceController(PlaceService placeService) {
        this.placeService = placeService;
    }

    @PostMapping
    @Operation(summary = "Crear un nuevo lugar turístico", description = "Registra un lugar con coordenadas geográficas válidas (-90 a 90 latitud, -180 a 180 longitud) y categoría asociada existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Lugar creado con éxito",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PlaceResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o coordenadas fuera de rango",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Categoría especificada no encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflicto en la creación del lugar",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PlaceResponse> createPlace(@Valid @RequestBody PlaceCreateRequest request) {
        PlaceResponse response = placeService.createPlace(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener lugar por ID", description = "Recupera un lugar turístico específico por su identificador único, incluyendo su categoría.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lugar encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PlaceResponse.class))),
            @ApiResponse(responseCode = "404", description = "Lugar no encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PlaceResponse> getPlace(
            @Parameter(description = "ID del lugar turístico", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(placeService.getPlace(id));
    }

    @GetMapping
    @Operation(summary = "Listar todos los lugares turísticos", description = "Devuelve el listado completo de lugares turísticos registrados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado recuperado con éxito",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = PlaceResponse.class))))
    })
    public ResponseEntity<List<PlaceResponse>> listPlaces() {
        return ResponseEntity.ok(placeService.listPlaces());
    }
}
