package com.alonso.explorersaga.adapters.out.persistence;

import com.alonso.explorersaga.application.port.out.SourceRepositoryPort;
import com.alonso.explorersaga.entity.Source;
import com.alonso.explorersaga.repository.SourceRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class JpaSourceRepositoryAdapter implements SourceRepositoryPort {

    private final SourceRepository sourceRepository;

    public JpaSourceRepositoryAdapter(SourceRepository sourceRepository) {
        this.sourceRepository = sourceRepository;
    }

    @Override
    public Source save(Source source) {
        return sourceRepository.save(source);
    }

    @Override
    public Optional<Source> findById(Long id) {
        return sourceRepository.findById(id);
    }

    @Override
    public Optional<Source> findByCode(String code) {
        return sourceRepository.findByCode(code);
    }

    @Override
    public List<Source> findAll() {
        return sourceRepository.findAll();
    }
}
