package com.alonso.explorersaga.application.port.out;

public record NormalizedPlaceData(
        String externalId,
        String sourceCode,
        String name,
        String description,
        String address,
        Double latitude,
        Double longitude,
        String categoryName
) {}
