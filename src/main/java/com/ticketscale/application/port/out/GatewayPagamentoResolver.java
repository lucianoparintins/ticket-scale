package com.ticketscale.application.port.out;

import com.ticketscale.domain.pagamento.MetodoPagamento;

public interface GatewayPagamentoResolver {
    GatewayPagamento resolver(MetodoPagamento metodo);
}
