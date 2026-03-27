package com.ticketscale.domain.pagamento;

public class MetodoNaoSuportadoException extends PagamentoException {
    public MetodoNaoSuportadoException(String mensagem) { super(mensagem); }
}
