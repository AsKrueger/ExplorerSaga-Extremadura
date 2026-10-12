package com.alonso.explorersaga.repository;

import com.alonso.explorersaga.entity.PlaceSource;
import com.alonso.explorersaga.entity.Source;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlaceSourceRepository extends JpaRepository<PlaceSource, Long> {
    Optional<PlaceSource> findBySourceAndExternalId(Source source, String externalId);
}
