package com.ticketscale.interfaces.rest.dashboard;

import com.ticketscale.domain.dashboard.MetricaVendas;
import java.math.BigDecimal;
import java.util.UUID;

public record DadosMetricaVendas(
    UUID eventoId,
    String eventoNome,
    long ingressosVendidos,
    BigDecimal receitaTotal,
    // Compat: campos usados pelo bundle antigo do admin (/admin) ainda em resources/static.
    String nomeEvento,
    long quantidadeVendida,
    BigDecimal valorTotal
) {
    public DadosMetricaVendas(MetricaVendas metrica) {
        this(
                metrica.eventoId(),
                metrica.eventoNome(),
                metrica.ingressosVendidos(),
                metrica.receitaTotal() != null ? metrica.receitaTotal() : BigDecimal.ZERO,
                metrica.eventoNome(),
                metrica.ingressosVendidos(),
                metrica.receitaTotal() != null ? metrica.receitaTotal() : BigDecimal.ZERO
        );
    }
}
