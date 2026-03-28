package com.ticketscale.application.usecase;

import com.ticketscale.domain.dashboard.DashboardRepository;
import com.ticketscale.domain.dashboard.FiltroDashboard;
import com.ticketscale.domain.dashboard.MetricasDashboard;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ObterMetricasDashboardTest {

    @Mock
    private DashboardRepository dashboardRepository;

    @InjectMocks
    private ObterMetricasDashboard useCase;

    @Test
    void deveObterMetricasDashboard() {
        // Given
        var filtro = new FiltroDashboard(null, null, null, 0, 100);
        var metricas = new MetricasDashboard(BigDecimal.valueOf(1000), 10, List.of(), 0.5);
        when(dashboardRepository.buscarMetricasDashboard(filtro)).thenReturn(metricas);

        // When
        var resultado = useCase.executar(filtro);

        // Then
        assertThat(resultado.receitaTotal()).isEqualByComparingTo("1000");
        assertThat(resultado.taxaConversao()).isEqualTo(0.5);
    }
}
