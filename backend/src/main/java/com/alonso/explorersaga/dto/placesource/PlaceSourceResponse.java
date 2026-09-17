package com.alonso.explorersaga.dto.placesource;

import com.alonso.explorersaga.dto.source.SourceResponse;
import java.time.OffsetDateTime;

public class PlaceSourceResponse {
    private Long id;
    private Long placeId;
    private SourceResponse source;
    private String externalId;
    private OffsetDateTime lastSync;

    public PlaceSourceResponse() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPlaceId() {
        return placeId;
    }

    public void setPlaceId(Long placeId) {
        this.placeId = placeId;
    }

    public SourceResponse getSource() {
        return source;
    }

    public void setSource(SourceResponse source) {
        this.source = source;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public OffsetDateTime getLastSync() {
        return lastSync;
    }

    public void setLastSync(OffsetDateTime lastSync) {
        this.lastSync = lastSync;
    }
}
