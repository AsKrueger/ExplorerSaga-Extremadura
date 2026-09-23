package com.alonso.explorersaga.application.port.in;

import com.alonso.explorersaga.dto.place.PlaceCreateRequest;
import com.alonso.explorersaga.dto.place.PlaceResponse;

import java.util.List;

public interface PlaceUseCase {
    PlaceResponse createPlace(PlaceCreateRequest request);
    PlaceResponse getPlace(Long id);
    List<PlaceResponse> listPlaces();
}
