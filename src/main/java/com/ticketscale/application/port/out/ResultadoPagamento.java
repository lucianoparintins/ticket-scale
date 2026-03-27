package com.ticketscale.application.port.out;

public record ResultadoPagamento(
    boolean sucesso,
    String transacaoExternaId,
    String mensagemErro
) {}
