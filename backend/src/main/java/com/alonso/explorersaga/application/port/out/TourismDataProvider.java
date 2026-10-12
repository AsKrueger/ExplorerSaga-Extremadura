package com.alonso.explorersaga.application.port.out;

import java.util.List;

public interface TourismDataProvider {
    List<NormalizedPlaceData> fetchTourismPlaces();
}
