package com.ticketscale.infrastructure.messaging;

import com.ticketscale.application.port.out.EventPublisher;
import com.ticketscale.domain.event.ReservaCriadaEvent;
import com.ticketscale.infrastructure.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQEventPublisher implements EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publicarReservaCriada(ReservaCriadaEvent evento) {
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE_TICKETSCALE_EVENTS,
            RabbitMQConfig.ROUTING_KEY_RESERVA_CRIADA,
            evento
        );
    }
    
    @Override
    public void publicarReservaExpiracao(ReservaCriadaEvent evento) {
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE_TICKETSCALE_EVENTS,
            RabbitMQConfig.ROUTING_KEY_RESERVA_EXPIRACAO,
            evento
        );
    }
}
