package com.alonso.explorersaga.service;

import com.alonso.explorersaga.application.port.out.SourceRepositoryPort;
import com.alonso.explorersaga.dto.source.SourceCreateRequest;
import com.alonso.explorersaga.dto.source.SourceResponse;
import com.alonso.explorersaga.entity.Source;
import com.alonso.explorersaga.exception.DuplicateResourceException;
import com.alonso.explorersaga.exception.InvalidDataException;
import com.alonso.explorersaga.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SourceServiceTest {

    @Mock
    private SourceRepositoryPort sourceRepositoryPort;

    @InjectMocks
    private SourceService sourceService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createSource_Success() {
        SourceCreateRequest request = new SourceCreateRequest("OSM", "OpenStreetMap", "Datos abiertos");
        Source saved = new Source("OSM", "OpenStreetMap");
        saved.setId(1L);
        saved.setDescription("Datos abiertos");

        when(sourceRepositoryPort.findByCode("OSM")).thenReturn(Optional.empty());
        when(sourceRepositoryPort.save(any(Source.class))).thenReturn(saved);

        SourceResponse response = sourceService.createSource(request);

        ArgumentCaptor<Source> captor = ArgumentCaptor.forClass(Source.class);
        verify(sourceRepositoryPort).save(captor.capture());
        assertThat(captor.getValue().getCode()).isEqualTo("OSM");
        assertThat(captor.getValue().getName()).isEqualTo("OpenStreetMap");
        assertThat(captor.getValue().getDescription()).isEqualTo("Datos abiertos");

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getCode()).isEqualTo("OSM");
        assertThat(response.getName()).isEqualTo("OpenStreetMap");
        assertThat(response.getDescription()).isEqualTo("Datos abiertos");
    }

    @Test
    void createSource_BlankCode_ThrowsInvalidData() {
        SourceCreateRequest request = new SourceCreateRequest("   ", "OpenStreetMap", null);

        assertThrows(InvalidDataException.class, () -> sourceService.createSource(request));
        verify(sourceRepositoryPort, never()).save(any(Source.class));
    }

    @Test
    void createSource_NullName_ThrowsInvalidData() {
        SourceCreateRequest request = new SourceCreateRequest("OSM", null, null);

        assertThrows(InvalidDataException.class, () -> sourceService.createSource(request));
        verify(sourceRepositoryPort, never()).save(any(Source.class));
    }

    @Test
    void createSource_DuplicateCode_ThrowsDuplicateResource() {
        SourceCreateRequest request = new SourceCreateRequest("OSM", "OpenStreetMap", null);
        when(sourceRepositoryPort.findByCode("OSM")).thenReturn(Optional.of(new Source("OSM", "OpenStreetMap")));

        assertThrows(DuplicateResourceException.class, () -> sourceService.createSource(request));
        verify(sourceRepositoryPort, never()).save(any(Source.class));
    }

    @Test
    void getSource_Found_ReturnsResponse() {
        Source entity = new Source("OSM", "OpenStreetMap");
        entity.setId(7L);
        when(sourceRepositoryPort.findById(7L)).thenReturn(Optional.of(entity));

        SourceResponse response = sourceService.getSource(7L);

        assertThat(response.getId()).isEqualTo(7L);
        assertThat(response.getCode()).isEqualTo("OSM");
    }

    @Test
    void getSource_NotFound_ThrowsResourceNotFound() {
        when(sourceRepositoryPort.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> sourceService.getSource(1L));
    }

    @Test
    void listSources_MapsAllEntities() {
        Source osm = new Source("OSM", "OpenStreetMap");
        osm.setId(1L);
        Source manual = new Source("MANUAL", "Carga manual");
        manual.setId(2L);
        when(sourceRepositoryPort.findAll()).thenReturn(List.of(osm, manual));

        List<SourceResponse> responses = sourceService.listSources();

        assertThat(responses).extracting(SourceResponse::getCode).containsExactly("OSM", "MANUAL");
        assertThat(responses).extracting(SourceResponse::getId).containsExactly(1L, 2L);
    }

    @Test
    void listSources_Empty_ReturnsEmptyList() {
        when(sourceRepositoryPort.findAll()).thenReturn(List.of());

        assertThat(sourceService.listSources()).isEmpty();
    }
}
