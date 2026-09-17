CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
);

CREATE TABLE sources (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT
);

CREATE TABLE places (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    address VARCHAR(500),
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    category_id BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_places_category FOREIGN KEY (category_id) REFERENCES categories (id)
);

CREATE TABLE place_sources (
    id BIGSERIAL PRIMARY KEY,
    place_id BIGINT NOT NULL,
    source_id BIGINT NOT NULL,
    external_id VARCHAR(255) NOT NULL,
    last_sync TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_place_sources_place FOREIGN KEY (place_id) REFERENCES places (id),
    CONSTRAINT fk_place_sources_source FOREIGN KEY (source_id) REFERENCES sources (id),
    CONSTRAINT uk_place_sources_source_external UNIQUE (source_id, external_id)
);

CREATE INDEX idx_places_category ON places(category_id);
CREATE INDEX idx_place_sources_place ON place_sources(place_id);
CREATE INDEX idx_place_sources_source ON place_sources(source_id);
