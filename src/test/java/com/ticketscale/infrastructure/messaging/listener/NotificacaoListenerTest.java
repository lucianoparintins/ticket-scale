package com.ticketscale.infrastructure.messaging.listener;

import com.ticketscale.domain.event.ReservaCriadaEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class NotificacaoListenerTest {

    private final NotificacaoListener listener = new NotificacaoListener();

    @Test
    void processarNotificacao_naoDeveLancarExcecaoAoLogarEvento() {
        ReservaCriadaEvent evento = new ReservaCriadaEvent("res-123", "usr-123", "lote-123");

        assertDoesNotThrow(() -> listener.processarNotificacao(evento));
    }
}
