package com.alonso.explorersaga.service;

import com.alonso.explorersaga.application.port.in.SourceUseCase;
import com.alonso.explorersaga.application.port.out.SourceRepositoryPort;
import com.alonso.explorersaga.dto.source.SourceCreateRequest;
import com.alonso.explorersaga.dto.source.SourceResponse;
import com.alonso.explorersaga.entity.Source;
import com.alonso.explorersaga.exception.DuplicateResourceException;
import com.alonso.explorersaga.exception.InvalidDataException;
import com.alonso.explorersaga.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SourceService implements SourceUseCase {

    private final SourceRepositoryPort sourceRepositoryPort;

    public SourceService(SourceRepositoryPort sourceRepositoryPort) {
        this.sourceRepositoryPort = sourceRepositoryPort;
    }

    @Override
    @Transactional
    public SourceResponse createSource(SourceCreateRequest request) {
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            throw new InvalidDataException("Source code is required");
        }
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new InvalidDataException("Source name is required");
        }

        if (sourceRepositoryPort.findByCode(request.getCode()).isPresent()) {
            throw new DuplicateResourceException("Source with code '" + request.getCode() + "' already exists");
        }

        Source source = new Source(request.getCode(), request.getName());
        source.setDescription(request.getDescription());
        Source saved = sourceRepositoryPort.save(source);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SourceResponse getSource(Long id) {
        return sourceRepositoryPort.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Source with ID " + id + " not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SourceResponse> listSources() {
        return sourceRepositoryPort.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private SourceResponse mapToResponse(Source entity) {
        return new SourceResponse(entity.getId(), entity.getCode(), entity.getName(), entity.getDescription());
    }
}
