package com.alonso.explorersaga.integration;

import com.alonso.explorersaga.dto.category.CategoryCreateRequest;
import com.alonso.explorersaga.dto.category.CategoryResponse;
import com.alonso.explorersaga.dto.place.PlaceCreateRequest;
import com.alonso.explorersaga.dto.place.PlaceResponse;
import com.alonso.explorersaga.dto.placesource.PlaceSourceCreateRequest;
import com.alonso.explorersaga.dto.source.SourceCreateRequest;
import com.alonso.explorersaga.dto.source.SourceResponse;
import com.alonso.explorersaga.repository.CategoryRepository;
import com.alonso.explorersaga.repository.PlaceRepository;
import com.alonso.explorersaga.repository.PlaceSourceRepository;
import com.alonso.explorersaga.repository.SourceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RestPersistenceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    private PlaceSourceRepository placeSourceRepository;

    @BeforeEach
    void cleanUp() {
        placeSourceRepository.deleteAll();
        placeRepository.deleteAll();
        sourceRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    void fullRestFlow_Success() throws Exception {
        // 1. POST Category
        CategoryCreateRequest catReq = new CategoryCreateRequest("MONUMENT", "History");
        MvcResult catResult = mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(catReq)))
                .andExpect(status().isCreated())
                .andReturn();
        CategoryResponse catRes = objectMapper.readValue(catResult.getResponse().getContentAsString(), CategoryResponse.class);

        // 2. POST Source
        SourceCreateRequest sourceReq = new SourceCreateRequest("GOOGLE", "Google Places", "API");
        MvcResult sourceResult = mockMvc.perform(post("/api/v1/sources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sourceReq)))
                .andExpect(status().isCreated())
                .andReturn();
        SourceResponse sourceRes = objectMapper.readValue(sourceResult.getResponse().getContentAsString(), SourceResponse.class);

        // 3. POST Place
        PlaceCreateRequest placeReq = new PlaceCreateRequest();
        placeReq.setName("Teatro Romano");
        placeReq.setCategoryId(catRes.getId());
        placeReq.setLatitude(38.9);
        placeReq.setLongitude(-6.3);
        MvcResult placeResult = mockMvc.perform(post("/api/v1/places")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(placeReq)))
                .andExpect(status().isCreated())
                .andReturn();
        PlaceResponse placeRes = objectMapper.readValue(placeResult.getResponse().getContentAsString(), PlaceResponse.class);

        // 4. POST PlaceSource
        PlaceSourceCreateRequest psReq = new PlaceSourceCreateRequest(placeRes.getId(), sourceRes.getId(), "EXT-123");
        mockMvc.perform(post("/api/v1/place-sources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(psReq)))
                .andExpect(status().isCreated());

        // 5. GET Place and Verify
        mockMvc.perform(get("/api/v1/places/" + placeRes.getId()))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    PlaceResponse finalPlace = objectMapper.readValue(result.getResponse().getContentAsString(), PlaceResponse.class);
                    assertThat(finalPlace.getName()).isEqualTo("Teatro Romano");
                    assertThat(finalPlace.getCategory().getName()).isEqualTo("MONUMENT");
                });

        // 6. Test Error: Duplicate PlaceSource
        mockMvc.perform(post("/api/v1/place-sources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(psReq)))
                .andExpect(status().isConflict());
    }

    @Test
    void validationError_ReturnsBadRequest() throws Exception {
        PlaceCreateRequest invalidReq = new PlaceCreateRequest();
        invalidReq.setName(""); // Blank
        invalidReq.setLatitude(100.0); // Out of range

        mockMvc.perform(post("/api/v1/places")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest());
    }
}
