package com.alonso.explorersaga.controller;

import com.alonso.explorersaga.dto.common.ErrorResponse;
import com.alonso.explorersaga.dto.source.SourceCreateRequest;
import com.alonso.explorersaga.dto.source.SourceResponse;
import com.alonso.explorersaga.service.SourceService;
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
@RequestMapping("/api/v1/sources")
@Tag(name = "Sources", description = "Gestión de fuentes de información externa")
public class SourceController {

    private final SourceService sourceService;

    public SourceController(SourceService sourceService) {
        this.sourceService = sourceService;
    }

    @PostMapping
    @Operation(summary = "Crear una nueva fuente", description = "Registra una fuente de datos externa única por su código.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Fuente creada con éxito",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SourceResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o faltantes",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflicto: ya existe una fuente con el mismo código",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SourceResponse> createSource(@Valid @RequestBody SourceCreateRequest request) {
        SourceResponse response = sourceService.createSource(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener fuente por ID", description = "Recupera una fuente de información específica por su identificador único.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fuente encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SourceResponse.class))),
            @ApiResponse(responseCode = "404", description = "Fuente no encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SourceResponse> getSource(
            @Parameter(description = "ID de la fuente", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(sourceService.getSource(id));
    }

    @GetMapping
    @Operation(summary = "Listar todas las fuentes", description = "Devuelve el listado completo de fuentes de información registradas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado recuperado con éxito",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = SourceResponse.class))))
    })
    public ResponseEntity<List<SourceResponse>> listSources() {
        return ResponseEntity.ok(sourceService.listSources());
    }
}
