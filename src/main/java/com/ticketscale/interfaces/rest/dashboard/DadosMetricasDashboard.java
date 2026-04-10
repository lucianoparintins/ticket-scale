package com.ticketscale.interfaces.rest.dashboard;

import com.ticketscale.domain.dashboard.MetricasDashboard;
import java.math.BigDecimal;
import java.util.List;

public record DadosMetricasDashboard(
    BigDecimal receitaTotal,
    long ingressosVendidos,
    List<DadosMetricaVendas> vendasPorEvento,
    double taxaConversao,
    // Compat: campos usados pelo bundle antigo do admin (/admin) ainda em resources/static.
    long totalVendas,
    long totalReservas,
    double ticketMedio
) {
    public DadosMetricasDashboard(MetricasDashboard metricas) {
        BigDecimal receita = metricas.receitaTotal() != null ? metricas.receitaTotal() : BigDecimal.ZERO;
        long vendas = metricas.ingressosVendidos();
        double ticketMedio = vendas > 0 ? receita.doubleValue() / vendas : 0.0;
        this(
            receita,
            metricas.ingressosVendidos(),
            metricas.vendasPorEvento().stream().map(DadosMetricaVendas::new).toList(),
            metricas.taxaConversao(),
            vendas,
            0L,
            ticketMedio
        );
    }
}
