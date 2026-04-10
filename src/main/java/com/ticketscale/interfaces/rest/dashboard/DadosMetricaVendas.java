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
        BigDecimal receita = metrica.receitaTotal() != null ? metrica.receitaTotal() : BigDecimal.ZERO;
        this(
                metrica.eventoId(),
                metrica.eventoNome(),
                metrica.ingressosVendidos(),
                receita,
                metrica.eventoNome(),
                metrica.ingressosVendidos(),
                receita
        );
    }
}
