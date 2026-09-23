package com.alonso.explorersaga.application.port.out;

import com.alonso.explorersaga.entity.Source;

import java.util.List;
import java.util.Optional;

public interface SourceRepositoryPort {
    Source save(Source source);
    Optional<Source> findById(Long id);
    Optional<Source> findByCode(String code);
    List<Source> findAll();
}
