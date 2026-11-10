package com.alonso.explorersaga.adapters.out.persistence;

import com.alonso.explorersaga.application.port.out.PlaceSourceRepositoryPort;
import com.alonso.explorersaga.entity.Place;
import com.alonso.explorersaga.entity.PlaceSource;
import com.alonso.explorersaga.entity.Source;
import com.alonso.explorersaga.repository.PlaceSourceRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class JpaPlaceSourceRepositoryAdapter implements PlaceSourceRepositoryPort {

    private final PlaceSourceRepository placeSourceRepository;

    public JpaPlaceSourceRepositoryAdapter(PlaceSourceRepository placeSourceRepository) {
        this.placeSourceRepository = placeSourceRepository;
    }

    @Override
    public PlaceSource saveAndFlush(PlaceSource placeSource) {
        return placeSourceRepository.saveAndFlush(placeSource);
    }

    @Override
    public Optional<PlaceSource> findBySourceAndExternalId(Source source, String externalId) {
        return placeSourceRepository.findBySourceAndExternalId(source, externalId);
    }

    @Override
    public long countByPlace(Place place) {
        return placeSourceRepository.countByPlace(place);
    }
}
