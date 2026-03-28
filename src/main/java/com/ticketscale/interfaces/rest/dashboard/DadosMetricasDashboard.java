package com.ticketscale.interfaces.rest.dashboard;

import com.ticketscale.domain.dashboard.MetricasDashboard;
import java.math.BigDecimal;
import java.util.List;

public record DadosMetricasDashboard(
    BigDecimal receitaTotal,
    long ingressosVendidos,
    List<DadosMetricaVendas> vendasPorEvento,
    double taxaConversao
) {
    public DadosMetricasDashboard(MetricasDashboard metricas) {
        this(
            metricas.receitaTotal(),
            metricas.ingressosVendidos(),
            metricas.vendasPorEvento().stream().map(DadosMetricaVendas::new).toList(),
            metricas.taxaConversao()
        );
    }
}
