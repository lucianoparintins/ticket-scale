package com.ticketscale.application.port.out;

import com.ticketscale.domain.event.CacheInvalidadoEvent;
import com.ticketscale.domain.event.PagamentoConfirmadoEvent;
import com.ticketscale.domain.event.ReservaCriadaEvent;
import com.ticketscale.domain.event.ReservaExpiradaEvent;

public interface EventPublisher {
    void publicarReservaCriada(ReservaCriadaEvent evento);
    void publicarReservaExpiracao(ReservaCriadaEvent evento);
    void publicarReservaExpirada(ReservaExpiradaEvent evento);
    void publicarPagamentoConfirmado(PagamentoConfirmadoEvent evento);
    void publicarInvalidacaoCache(CacheInvalidadoEvent evento);
}
