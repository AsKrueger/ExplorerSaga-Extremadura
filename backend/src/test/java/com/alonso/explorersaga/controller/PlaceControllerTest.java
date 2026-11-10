package com.alonso.explorersaga.controller;

import com.alonso.explorersaga.application.port.in.ImportResult;
import com.alonso.explorersaga.application.port.in.ImportTourismPlacesUseCase;
import com.alonso.explorersaga.application.port.in.PlaceUseCase;
import com.alonso.explorersaga.dto.category.CategoryResponse;
import com.alonso.explorersaga.dto.place.PlaceCreateRequest;
import com.alonso.explorersaga.dto.place.PlaceResponse;
import com.alonso.explorersaga.exception.ExternalProviderException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlaceController.class)
class PlaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PlaceUseCase placeUseCase;

    @MockBean
    private ImportTourismPlacesUseCase importTourismPlacesUseCase;

    @Test
    void createPlace_Success() throws Exception {
        PlaceCreateRequest request = new PlaceCreateRequest();
        request.setName("Teatro");
        request.setCategoryId(1L);
        request.setLatitude(38.0);
        request.setLongitude(-6.0);

        PlaceResponse response = new PlaceResponse();
        response.setId(1L);
        response.setName("Teatro");
        response.setCategory(new CategoryResponse(1L, "CAT", "D"));

        when(placeUseCase.createPlace(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/places")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void createPlace_InvalidCoordinates_400() throws Exception {
        PlaceCreateRequest request = new PlaceCreateRequest();
        request.setName("Teatro");
        request.setCategoryId(1L);
        request.setLatitude(100.0); // Invalid
        request.setLongitude(-6.0);

        mockMvc.perform(post("/api/v1/places")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getPlace_Success() throws Exception {
        PlaceResponse response = new PlaceResponse();
        response.setId(1L);
        response.setName("P");
        when(placeUseCase.getPlace(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/places/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("P"));
    }

    @Test
    void listPlaces_Success() throws Exception {
        PlaceResponse response = new PlaceResponse();
        response.setName("P");
        when(placeUseCase.listPlaces(any())).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/places"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("P"));
    }

    @Test
    void importPlaces_Success() throws Exception {
        ImportResult importResult = new ImportResult(10, 8, 1, 1, 0, 0);
        when(importTourismPlacesUseCase.importPlaces()).thenReturn(importResult);

        mockMvc.perform(post("/api/v1/places/import"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.processed").value(10))
                .andExpect(jsonPath("$.inserted").value(8))
                .andExpect(jsonPath("$.updated").value(1));
    }

    @Test
    void importPlaces_ProviderUnavailable_ReturnsBadGateway() throws Exception {
        when(importTourismPlacesUseCase.importPlaces())
                .thenThrow(new ExternalProviderException("Overpass API no está disponible"));

        mockMvc.perform(post("/api/v1/places/import"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.message").value("Overpass API no está disponible"));
    }
}
