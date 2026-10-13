package com.alonso.explorersaga.adapters.out.persistence;

import com.alonso.explorersaga.application.port.out.PlaceRepositoryPort;
import com.alonso.explorersaga.entity.Place;
import com.alonso.explorersaga.repository.PlaceRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class JpaPlaceRepositoryAdapter implements PlaceRepositoryPort {

    private final PlaceRepository placeRepository;

    public JpaPlaceRepositoryAdapter(PlaceRepository placeRepository) {
        this.placeRepository = placeRepository;
    }

    @Override
    public Place save(Place place) {
        return placeRepository.save(place);
    }

    @Override
    public Optional<Place> findById(Long id) {
        return placeRepository.findById(id);
    }

    @Override
    public List<Place> findAll() {
        return placeRepository.findAll();
    }

    @Override
    public List<Place> findByCategoryNameIgnoreCase(String name) {
        return placeRepository.findByCategoryNameContainingIgnoreCase(name);
    }
}
