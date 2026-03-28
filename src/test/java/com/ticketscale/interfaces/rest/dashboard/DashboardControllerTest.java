package com.ticketscale.interfaces.rest.dashboard;

import com.ticketscale.application.usecase.CalcularReceitaTotal;
import com.ticketscale.application.usecase.GerarRelatorioVendasPorEvento;
import com.ticketscale.application.usecase.ObterMetricasDashboard;
import com.ticketscale.domain.dashboard.MetricaVendas;
import com.ticketscale.domain.dashboard.MetricasDashboard;
import com.ticketscale.domain.dashboard.RelatorioReceita;
import com.ticketscale.infrastructure.security.SecurityFilter;
import com.ticketscale.infrastructure.security.TokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GerarRelatorioVendasPorEvento gerarRelatorioVendasPorEvento;

    @MockitoBean
    private CalcularReceitaTotal calcularReceitaTotal;

    @MockitoBean
    private ObterMetricasDashboard obterMetricasDashboard;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private SecurityFilter securityFilter;

    @Test
    @DisplayName("Deve retornar 200 ao buscar vendas por evento")
    void vendasPorEvento_deveRetornarStatus200() throws Exception {
        var metrica = new MetricaVendas(UUID.randomUUID(), "Evento Teste", 10L, BigDecimal.valueOf(100));
        when(gerarRelatorioVendasPorEvento.executar(any())).thenReturn(List.of(metrica));

        mockMvc.perform(get("/dashboard/vendas-por-evento"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventoNome").value("Evento Teste"))
                .andExpect(jsonPath("$[0].ingressosVendidos").value(10));
    }

    @Test
    @DisplayName("Deve retornar 200 ao buscar receita total")
    void receitaTotal_deveRetornarStatus200() throws Exception {
        var relatorio = new RelatorioReceita(BigDecimal.valueOf(1000), 50, LocalDateTime.now(), LocalDateTime.now());
        when(calcularReceitaTotal.executar(any())).thenReturn(relatorio);

        mockMvc.perform(get("/dashboard/receita-total"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1000))
                .andExpect(jsonPath("$.quantidadeVendas").value(50));
    }

    @Test
    @DisplayName("Deve retornar 200 ao buscar métricas consolidadas")
    void metricas_deveRetornarStatus200() throws Exception {
        var metricas = new MetricasDashboard(BigDecimal.valueOf(1000), 10L, List.of(), 0.8);
        when(obterMetricasDashboard.executar(any())).thenReturn(metricas);

        mockMvc.perform(get("/dashboard/metricas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.receitaTotal").value(1000))
                .andExpect(jsonPath("$.taxaConversao").value(0.8));
    }
}
