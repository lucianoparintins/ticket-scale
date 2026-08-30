package com.ticketscale.interfaces.rest.dashboard;

import com.ticketscale.domain.dashboard.RelatorioReceita;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DadosRelatorioReceita(
    BigDecimal total,
    int quantidadeVendas,
    LocalDateTime periodoInicio,
    LocalDateTime periodoFim,
    // Compat: campo usado pelo bundle antigo do admin (/admin) ainda em resources/static.
    BigDecimal valorTotal
) {
    public DadosRelatorioReceita(RelatorioReceita relatorio) {
        this(
            relatorio.total() != null ? relatorio.total() : BigDecimal.ZERO,
            relatorio.quantidadeVendas(),
            relatorio.periodoInicio(),
            relatorio.periodoFim(),
            relatorio.total() != null ? relatorio.total() : BigDecimal.ZERO
        );
    }
}
