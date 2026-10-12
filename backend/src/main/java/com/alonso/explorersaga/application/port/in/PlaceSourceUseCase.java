package com.alonso.explorersaga.application.port.in;

import com.alonso.explorersaga.dto.placesource.PlaceSourceCreateRequest;
import com.alonso.explorersaga.dto.placesource.PlaceSourceResponse;

public interface PlaceSourceUseCase {
    PlaceSourceResponse addPlaceSource(PlaceSourceCreateRequest request);
}
