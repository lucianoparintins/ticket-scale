package com.ticketscale.domain.event;

public class ReservaCriadaEvent {
    
    private final String reservaId;
    private final String usuarioId;
    private final String loteId;

    public ReservaCriadaEvent(String reservaId, String usuarioId, String loteId) {
        this.reservaId = reservaId;
        this.usuarioId = usuarioId;
        this.loteId = loteId;
    }

    public String getReservaId() {
        return reservaId;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public String getLoteId() {
        return loteId;
    }
}
