package com.ticketscale.domain.event;

/**
 * Evento de domínio publicado quando uma reserva tem seu prazo expirado e é cancelada.
 */
public class ReservaExpiradaEvent {

    private final String reservaId;
    private final String ingressoId;
    private final String loteId;
    private final String usuarioId;

    public ReservaExpiradaEvent(String reservaId, String ingressoId, String loteId, String usuarioId) {
        this.reservaId = reservaId;
        this.ingressoId = ingressoId;
        this.loteId = loteId;
        this.usuarioId = usuarioId;
    }

    public String getReservaId() {
        return reservaId;
    }

    public String getIngressoId() {
        return ingressoId;
    }

    public String getLoteId() {
        return loteId;
    }

    public String getUsuarioId() {
        return usuarioId;
    }
}
