package com.alonso.explorersaga.controller;

import com.alonso.explorersaga.dto.source.SourceCreateRequest;
import com.alonso.explorersaga.dto.source.SourceResponse;
import com.alonso.explorersaga.service.SourceService;
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

@WebMvcTest(SourceController.class)
class SourceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SourceService sourceService;

    @Test
    void createSource_Success() throws Exception {
        SourceCreateRequest request = new SourceCreateRequest("S1", "Name", "Desc");
        SourceResponse response = new SourceResponse(1L, "S1", "Name", "Desc");

        when(sourceService.createSource(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/sources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("S1"));
    }

    @Test
    void getSource_Success() throws Exception {
        when(sourceService.getSource(1L)).thenReturn(new SourceResponse(1L, "S1", "N", "D"));

        mockMvc.perform(get("/api/v1/sources/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("S1"));
    }

    @Test
    void listSources_Success() throws Exception {
        when(sourceService.listSources()).thenReturn(List.of(new SourceResponse(1L, "S1", "N", "D")));

        mockMvc.perform(get("/api/v1/sources"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("S1"));
    }
}
