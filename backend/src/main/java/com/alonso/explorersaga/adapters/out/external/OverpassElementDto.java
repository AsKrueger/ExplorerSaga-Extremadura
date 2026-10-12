package com.alonso.explorersaga.adapters.out.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OverpassElementDto {
    private Long id;
    private String type;
    private Double lat;
    private Double lon;
    @JsonProperty("center")
    private OverpassCenterDto center;
    private Map<String, String> tags;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Double getLat() { return lat != null ? lat : (center != null ? center.getLat() : null); }
    public void setLat(Double lat) { this.lat = lat; }
    public Double getLon() { return lon != null ? lon : (center != null ? center.getLon() : null); }
    public void setLon(Double lon) { this.lon = lon; }
    public OverpassCenterDto getCenter() { return center; }
    public void setCenter(OverpassCenterDto center) { this.center = center; }
    public Map<String, String> getTags() { return tags; }
    public void setTags(Map<String, String> tags) { this.tags = tags; }
}
