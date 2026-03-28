package com.ticketscale.application.usecase;

import com.ticketscale.domain.dashboard.DashboardRepository;
import com.ticketscale.domain.dashboard.FiltroDashboard;
import com.ticketscale.domain.dashboard.MetricaVendas;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GerarRelatorioVendasPorEventoTest {

    @Mock
    private DashboardRepository dashboardRepository;

    @InjectMocks
    private GerarRelatorioVendasPorEvento useCase;

    @Test
    void deveGerarRelatorioVendasPorEvento() {
        // Given
        var filtro = new FiltroDashboard(null, null, null, 0, 100);
        var metricas = List.of(new MetricaVendas(UUID.randomUUID(), "Evento A", 100L, BigDecimal.valueOf(1000)));
        when(dashboardRepository.buscarVendasPorEvento(filtro)).thenReturn(metricas);

        // When
        var resultado = useCase.executar(filtro);

        // Then
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).eventoNome()).isEqualTo("Evento A");
    }
}
