package com.alonso.explorersaga.dto.placesource;

public class PlaceSourceCreateRequest {
    private Long placeId;
    private Long sourceId;
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
