package com.ticketscale.interfaces.rest.dashboard;

import com.ticketscale.domain.dashboard.MetricaVendas;
import java.math.BigDecimal;
import java.util.UUID;

public record DadosMetricaVendas(
    UUID eventoId,
    String eventoNome,
    long ingressosVendidos,
    BigDecimal receitaTotal
) {
    public DadosMetricaVendas(MetricaVendas metrica) {
        this(metrica.eventoId(), metrica.eventoNome(), metrica.ingressosVendidos(), metrica.receitaTotal());
    }
}
