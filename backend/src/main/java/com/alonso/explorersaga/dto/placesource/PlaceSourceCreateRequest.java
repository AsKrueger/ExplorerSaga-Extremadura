package com.alonso.explorersaga.dto.placesource;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class PlaceSourceCreateRequest {
    @NotNull
    private Long placeId;

    @NotNull
    private Long sourceId;

    @NotBlank
    @Size(max = 255)
    private String externalId;

    public PlaceSourceCreateRequest() {}

    public PlaceSourceCreateRequest(Long placeId, Long sourceId, String externalId) {
        this.placeId = placeId;
        this.sourceId = sourceId;
        this.externalId = externalId;
    }

    public Long getPlaceId() {
        return placeId;
    }

    public void setPlaceId(Long placeId) {
        this.placeId = placeId;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }
}
