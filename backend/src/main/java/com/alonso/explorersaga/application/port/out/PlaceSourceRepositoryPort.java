package com.alonso.explorersaga.application.port.out;

import com.alonso.explorersaga.entity.Place;
import com.alonso.explorersaga.entity.PlaceSource;
import com.alonso.explorersaga.entity.Source;

import java.util.Optional;

public interface PlaceSourceRepositoryPort {
    PlaceSource saveAndFlush(PlaceSource placeSource);
    Optional<PlaceSource> findBySourceAndExternalId(Source source, String externalId);
    long countByPlace(Place place);
}
