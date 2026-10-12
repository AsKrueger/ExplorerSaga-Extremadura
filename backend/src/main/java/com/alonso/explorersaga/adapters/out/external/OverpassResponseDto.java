package com.alonso.explorersaga.adapters.out.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OverpassResponseDto {
    private List<OverpassElementDto> elements;

    public List<OverpassElementDto> getElements() {
        return elements;
    }

    public void setElements(List<OverpassElementDto> elements) {
        this.elements = elements;
    }
}
