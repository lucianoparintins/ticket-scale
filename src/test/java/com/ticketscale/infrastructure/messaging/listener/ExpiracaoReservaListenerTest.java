package com.ticketscale.infrastructure.messaging.listener;

import com.ticketscale.application.usecase.ExpirarReservaUseCase;
import com.ticketscale.domain.event.ReservaCriadaEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ExpiracaoReservaListenerTest {

    @Mock
    private ExpirarReservaUseCase expirarReservaUseCase;

    @InjectMocks
    private ExpiracaoReservaListener listener;

    @Test
    void processarExpiracao_deveChamarUseCaseComSucesso() {
        UUID reservaId = UUID.randomUUID();
        ReservaCriadaEvent evento = new ReservaCriadaEvent(reservaId.toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString());

        listener.processarExpiracao(evento);

        verify(expirarReservaUseCase).executar(reservaId);
    }
}
