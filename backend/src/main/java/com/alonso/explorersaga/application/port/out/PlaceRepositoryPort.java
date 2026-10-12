package com.alonso.explorersaga.application.port.out;

import com.alonso.explorersaga.entity.Place;

import java.util.List;
import java.util.Optional;

public interface PlaceRepositoryPort {
    Place save(Place place);
    Optional<Place> findById(Long id);
    List<Place> findAll();
    List<Place> findByCategoryNameIgnoreCase(String name);
}
