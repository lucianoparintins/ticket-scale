package com.ticketscale.infrastructure.messaging.listener;

import com.ticketscale.domain.event.ReservaCriadaEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ExpiracaoReservaListenerTest {

    private final ExpiracaoReservaListener listener = new ExpiracaoReservaListener();

    @Test
    void processarExpiracao_naoDeveLancarExcecaoAoTratarEvento() {
        ReservaCriadaEvent evento = new ReservaCriadaEvent("res-123", "usr-123", "lote-123");

        assertDoesNotThrow(() -> listener.processarExpiracao(evento));
    }
}
