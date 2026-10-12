package com.alonso.explorersaga.controller;

import com.alonso.explorersaga.dto.placesource.PlaceSourceCreateRequest;
import com.alonso.explorersaga.dto.placesource.PlaceSourceResponse;
import com.alonso.explorersaga.exception.DuplicateResourceException;
import com.alonso.explorersaga.service.PlaceSourceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlaceSourceController.class)
class PlaceSourceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PlaceSourceService placeSourceService;

    @Test
    void addPlaceSource_Success() throws Exception {
        PlaceSourceCreateRequest request = new PlaceSourceCreateRequest(1L, 2L, "EXT");
        PlaceSourceResponse response = new PlaceSourceResponse();
        response.setId(1L);
        response.setExternalId("EXT");

        when(placeSourceService.addPlaceSource(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/place-sources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.externalId").value("EXT"));
    }

    @Test
    void addPlaceSource_Duplicate_409() throws Exception {
        PlaceSourceCreateRequest request = new PlaceSourceCreateRequest(1L, 2L, "EXT");
        when(placeSourceService.addPlaceSource(any())).thenThrow(new DuplicateResourceException("Dup"));

        mockMvc.perform(post("/api/v1/place-sources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }
}
