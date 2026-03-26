package com.ticketscale.infrastructure.messaging;

import com.ticketscale.domain.event.ReservaCriadaEvent;
import com.ticketscale.infrastructure.config.RabbitMQConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RabbitMQEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private RabbitMQEventPublisher publisher;

    @Test
    void publicarReservaCriada_deveEnviarMensagemParaAExchangeCorreta() {
        ReservaCriadaEvent evento = new ReservaCriadaEvent("res-123", "usr-123", "lote-123");

        publisher.publicarReservaCriada(evento);

        verify(rabbitTemplate).convertAndSend(
                RabbitMQConfig.EXCHANGE_TICKETSCALE_EVENTS,
                RabbitMQConfig.ROUTING_KEY_RESERVA_CRIADA,
                evento
        );
    }

    @Test
    void publicarReservaExpiracao_deveEnviarMensagemParaAExchangeCorreta() {
        ReservaCriadaEvent evento = new ReservaCriadaEvent("res-123", "usr-123", "lote-123");

        publisher.publicarReservaExpiracao(evento);

        verify(rabbitTemplate).convertAndSend(
                RabbitMQConfig.EXCHANGE_TICKETSCALE_EVENTS,
                RabbitMQConfig.ROUTING_KEY_RESERVA_EXPIRACAO,
                evento
        );
    }
}
