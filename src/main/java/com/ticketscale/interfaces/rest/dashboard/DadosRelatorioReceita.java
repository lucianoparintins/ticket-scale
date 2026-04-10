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
        BigDecimal total = relatorio.total() != null ? relatorio.total() : BigDecimal.ZERO;
        this(total, relatorio.quantidadeVendas(), relatorio.periodoInicio(), relatorio.periodoFim(), total);
    }
}
