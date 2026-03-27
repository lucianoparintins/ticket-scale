package com.ticketscale.domain.pagamento;

public class ReservaNaoEncontradaException extends PagamentoException {
    public ReservaNaoEncontradaException(String mensagem) { super(mensagem); }
}
