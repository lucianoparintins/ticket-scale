package com.ticketscale.infrastructure.persistence.dashboard;

import com.ticketscale.domain.dashboard.FiltroDashboard;
import com.ticketscale.domain.dashboard.RelatorioReceita;
import com.ticketscale.domain.dashboard.DashboardRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class DashboardRepositoryPostgresIT {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("ticketscale")
            .withUsername("ticketscale")
            .withPassword("ticketscale");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);

        // Garante schema no Postgres e nao executa data.sql (que e especifico do dev).
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.sql.init.mode", () -> "never");
    }

    @Autowired
    private DashboardRepository dashboardRepository;

    @Test
    @DisplayName("Nao deve falhar no PostgreSQL quando dataInicio/dataFim sao nulos (evita parametro sem tipo)")
    void calcularReceitaTotal_comDatasNulas_naoDeveFalhar() {
        var filtro = new FiltroDashboard(null, null, null, 0, 100);

        RelatorioReceita relatorio = assertDoesNotThrow(() -> dashboardRepository.calcularReceitaTotal(filtro));
        assertNotNull(relatorio);
    }
}

