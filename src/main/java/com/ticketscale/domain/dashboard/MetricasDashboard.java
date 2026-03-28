package com.ticketscale.domain.dashboard;

import java.math.BigDecimal;
import java.util.List;

public record MetricasDashboard(
    BigDecimal receitaTotal,
    long ingressosVendidos,
    List<MetricaVendas> vendasPorEvento,
    double taxaConversao
) {
}
