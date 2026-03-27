package com.ticketscale.application.port.out;

import com.ticketscale.domain.pagamento.MetodoPagamento;

public interface GatewayPagamento {
    ResultadoPagamento processarPagamento(SolicitacaoPagamento solicitacao);
    MetodoPagamento getMetodoSuportado();
}
