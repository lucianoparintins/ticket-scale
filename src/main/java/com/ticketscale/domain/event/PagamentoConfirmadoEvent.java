package com.ticketscale.domain.event;

public class PagamentoConfirmadoEvent {
    private final String reservaId;
    private final String pagamentoId;
    private final String valor;
    private final String metodoPagamento;

    public PagamentoConfirmadoEvent(String reservaId, String pagamentoId,
                                     String valor, String metodoPagamento) {
        this.reservaId = reservaId;
        this.pagamentoId = pagamentoId;
        this.valor = valor;
        this.metodoPagamento = metodoPagamento;
    }

    public String getReservaId() { return reservaId; }
    public String getPagamentoId() { return pagamentoId; }
    public String getValor() { return valor; }
    public String getMetodoPagamento() { return metodoPagamento; }
}
