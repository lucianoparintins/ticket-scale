package com.ticketscale.application.usecase;

import com.ticketscale.domain.dashboard.DashboardRepository;
import com.ticketscale.domain.dashboard.FiltroDashboard;
import com.ticketscale.domain.dashboard.RelatorioReceita;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalcularReceitaTotalTest {

    @Mock
    private DashboardRepository dashboardRepository;

    @InjectMocks
    private CalcularReceitaTotal useCase;

    @Test
    void deveCalcularReceitaTotal() {
        // Given
        var filtro = new FiltroDashboard(null, null, null, 0, 100);
        var relatorio = new RelatorioReceita(BigDecimal.valueOf(5000), 50, LocalDateTime.now(), LocalDateTime.now());
        when(dashboardRepository.calcularReceitaTotal(filtro)).thenReturn(relatorio);

        // When
        var resultado = useCase.executar(filtro);

        // Then
        assertThat(resultado.total()).isEqualByComparingTo("5000");
        assertThat(resultado.quantidadeVendas()).isEqualTo(50);
    }
}
