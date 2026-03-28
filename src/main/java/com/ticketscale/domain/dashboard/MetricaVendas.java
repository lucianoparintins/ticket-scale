package com.ticketscale.domain.dashboard;

import java.math.BigDecimal;
import java.util.UUID;

public record MetricaVendas(
    UUID eventoId,
    String eventoNome,
    long ingressosVendidos,
    BigDecimal receitaTotal
) {
    public MetricaVendas {
        if (eventoId == null) {
            throw new IllegalArgumentException("Evento ID é obrigatório.");
        }
        if (eventoNome == null || eventoNome.isBlank()) {
            throw new IllegalArgumentException("Nome do evento é obrigatório.");
        }
        if (ingressosVendidos < 0) {
            throw new IllegalArgumentException("Quantidade vendida não pode ser negativa.");
        }
        if (receitaTotal == null || receitaTotal.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Receita não pode ser nula ou negativa.");
        }
    }
}
