package com.ticketscale.application.port.out;

import com.ticketscale.domain.pagamento.DadosMetodoPagamento;
import com.ticketscale.domain.pagamento.MetodoPagamento;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public record SolicitacaoPagamento(
    UUID reservaId,
    BigDecimal valor,
    MetodoPagamento metodoPagamento,
    DadosMetodoPagamento dadosMetodo
) {
    public SolicitacaoPagamento {
        Objects.requireNonNull(dadosMetodo, "Dados do método de pagamento são obrigatórios.");
    }
}
