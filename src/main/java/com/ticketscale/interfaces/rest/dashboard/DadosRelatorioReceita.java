package com.ticketscale.interfaces.rest.dashboard;

import com.ticketscale.domain.dashboard.RelatorioReceita;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DadosRelatorioReceita(
    BigDecimal total,
    int quantidadeVendas,
    LocalDateTime periodoInicio,
    LocalDateTime periodoFim
) {
    public DadosRelatorioReceita(RelatorioReceita relatorio) {
        this(relatorio.total(), relatorio.quantidadeVendas(), relatorio.periodoInicio(), relatorio.periodoFim());
    }
}
