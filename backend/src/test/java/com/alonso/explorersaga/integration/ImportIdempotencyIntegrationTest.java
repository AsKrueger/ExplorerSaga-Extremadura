package com.alonso.explorersaga.integration;

import com.alonso.explorersaga.application.port.in.ImportResult;
import com.alonso.explorersaga.application.port.in.ImportTourismPlacesUseCase;
import com.alonso.explorersaga.application.port.out.NormalizedPlaceData;
import com.alonso.explorersaga.application.port.out.TourismDataProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

// Sin @Transactional a nivel de clase: cada importación se ejecuta en su propia transacción,
// como en producción, de modo que las relaciones LAZY se cargan como proxies de Hibernate
@SpringBootTest
@ActiveProfiles("test")
class ImportIdempotencyIntegrationTest {

    private static final String SOURCE_CODE = "IT_IMPORT";

    @MockBean
    private TourismDataProvider tourismDataProvider;

    @Autowired
    private ImportTourismPlacesUseCase importUseCase;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void cleanUp() {
        jdbcTemplate.update("DELETE FROM place_sources WHERE source_id IN (SELECT id FROM sources WHERE code = ?)", SOURCE_CODE);
        jdbcTemplate.update("DELETE FROM places WHERE name = 'Bar de Prueba Integración'");
        jdbcTemplate.update("DELETE FROM sources WHERE code = ?", SOURCE_CODE);
    }

    @Test
    void importingTwice_withSameData_changesNothingTheSecondTime() {
        when(tourismDataProvider.fetchTourismPlaces()).thenReturn(List.of(
                new NormalizedPlaceData("node/1", SOURCE_CODE, "Bar de Prueba Integración", null, "Calle Falsa",
                        38.9100, -6.3400, "bar"),
                // Coincide con "Templo de Diana" del seed (V2) a pocos metros: se enlaza, no se duplica
                new NormalizedPlaceData("way/2", SOURCE_CODE, "TEMPLO DE DIANA", null, null,
                        38.917100, -6.344600, "monumento")
        ));

        ImportResult first = importUseCase.importPlaces();
        ImportResult second = importUseCase.importPlaces();

        assertThat(first.inserted()).isEqualTo(1);
        assertThat(first.merged()).isEqualTo(1);
        assertThat(second.inserted()).isZero();
        assertThat(second.merged()).isZero();
        assertThat(second.updated()).isZero();
        assertThat(second.skipped()).isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM places WHERE LOWER(name) = 'templo de diana'", Integer.class)).isEqualTo(1);
    }
}
