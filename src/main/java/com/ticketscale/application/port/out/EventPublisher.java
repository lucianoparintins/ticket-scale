package com.ticketscale.application.port.out;

import com.ticketscale.domain.event.CacheInvalidadoEvent;
import com.ticketscale.domain.event.PagamentoConfirmadoEvent;
import com.ticketscale.domain.event.ReservaCriadaEvent;

public interface EventPublisher {
    void publicarReservaCriada(ReservaCriadaEvent evento);
    void publicarReservaExpiracao(ReservaCriadaEvent evento);
    void publicarPagamentoConfirmado(PagamentoConfirmadoEvent evento);
    void publicarInvalidacaoCache(CacheInvalidadoEvent evento);
}

