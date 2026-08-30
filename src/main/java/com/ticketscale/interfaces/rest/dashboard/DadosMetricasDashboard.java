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
        this(
            metricas.receitaTotal() != null ? metricas.receitaTotal() : BigDecimal.ZERO,
            metricas.ingressosVendidos(),
            metricas.vendasPorEvento().stream().map(DadosMetricaVendas::new).toList(),
            metricas.taxaConversao(),
            metricas.ingressosVendidos(),
            0L,
            metricas.ingressosVendidos() > 0 && metricas.receitaTotal() != null
                ? metricas.receitaTotal().doubleValue() / metricas.ingressosVendidos()
                : 0.0
        );
    }
}
