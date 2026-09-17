package com.alonso.explorersaga.integration;

import com.alonso.explorersaga.dto.category.CategoryCreateRequest;
import com.alonso.explorersaga.dto.category.CategoryResponse;
import com.alonso.explorersaga.dto.place.PlaceCreateRequest;
import com.alonso.explorersaga.dto.place.PlaceResponse;
import com.alonso.explorersaga.dto.placesource.PlaceSourceCreateRequest;
import com.alonso.explorersaga.dto.placesource.PlaceSourceResponse;
import com.alonso.explorersaga.dto.source.SourceCreateRequest;
import com.alonso.explorersaga.dto.source.SourceResponse;
import com.alonso.explorersaga.service.CategoryService;
import com.alonso.explorersaga.service.PlaceService;
import com.alonso.explorersaga.service.PlaceSourceService;
import com.alonso.explorersaga.service.SourceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ApplicationLayerIntegrationTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private PlaceService placeService;

    @Autowired
    private SourceService sourceService;

    @Autowired
    private PlaceSourceService placeSourceService;

    @Test
    void fullApplicationFlow_Success() {
        // 1. Create Category
        CategoryResponse catRes = categoryService.createCategory(new CategoryCreateRequest("MONUMENT", "Historical places"));
        assertThat(catRes.getId()).isNotNull();

        // 2. Create Place
        PlaceCreateRequest placeReq = new PlaceCreateRequest();
        placeReq.setName("Teatro Romano");
        placeReq.setCategoryId(catRes.getId());
        placeReq.setLatitude(38.9153);
        placeReq.setLongitude(-6.3386);
        placeReq.setAddress("Mérida");
        
        PlaceResponse placeRes = placeService.createPlace(placeReq);
        assertThat(placeRes.getId()).isNotNull();
        assertThat(placeRes.getCategory().getName()).isEqualTo("MONUMENT");

        // 3. Create Sources
        SourceResponse googleRes = sourceService.createSource(new SourceCreateRequest("GOOGLE", "Google", "API"));
        SourceResponse openDataRes = sourceService.createSource(new SourceCreateRequest("OPEN_DATA", "OpenData", "Web"));

        // 4. Associate Sources
        PlaceSourceResponse ps1 = placeSourceService.addPlaceSource(new PlaceSourceCreateRequest(placeRes.getId(), googleRes.getId(), "G123"));
        PlaceSourceResponse ps2 = placeSourceService.addPlaceSource(new PlaceSourceCreateRequest(placeRes.getId(), openDataRes.getId(), "OD123"));

        assertThat(ps1.getExternalId()).isEqualTo("G123");
        assertThat(ps2.getExternalId()).isEqualTo("OD123");
        assertThat(ps1.getSource().getCode()).isEqualTo("GOOGLE");
    }
}
