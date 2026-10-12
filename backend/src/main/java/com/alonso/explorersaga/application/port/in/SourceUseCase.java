package com.alonso.explorersaga.application.port.in;

import com.alonso.explorersaga.dto.source.SourceCreateRequest;
import com.alonso.explorersaga.dto.source.SourceResponse;

import java.util.List;

public interface SourceUseCase {
    SourceResponse createSource(SourceCreateRequest request);
    SourceResponse getSource(Long id);
    List<SourceResponse> listSources();
}
