package com.ticketscale.domain.dashboard;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RelatorioReceita(
    BigDecimal total,
    int quantidadeVendas,
    LocalDateTime periodoInicio,
    LocalDateTime periodoFim
) {
    public RelatorioReceita {
        if (total == null || total.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Total não pode ser nulo ou negativo.");
        }
        if (quantidadeVendas < 0) {
            throw new IllegalArgumentException("Quantidade de vendas não pode ser negativa.");
        }
    }
}
