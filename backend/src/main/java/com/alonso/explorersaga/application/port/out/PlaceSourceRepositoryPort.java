package com.alonso.explorersaga.application.port.out;

import com.alonso.explorersaga.entity.PlaceSource;

public interface PlaceSourceRepositoryPort {
    PlaceSource saveAndFlush(PlaceSource placeSource);
}
