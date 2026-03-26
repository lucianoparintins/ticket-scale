package com.ticketscale.infrastructure.messaging.listener;

import com.ticketscale.domain.event.ReservaCriadaEvent;
import com.ticketscale.infrastructure.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ExpiracaoReservaListener {

    private static final Logger log = LoggerFactory.getLogger(ExpiracaoReservaListener.class);

    @RabbitListener(queues = RabbitMQConfig.QUEUE_RESERVATIONS_EXPIRATION)
    public void processarExpiracao(ReservaCriadaEvent evento) {
        log.info("Iniciando verificação de expiração para a reserva [{}]", evento.getReservaId());
        // Aqui deve entrar a lógica para expirar a reserva caso o pagamento não tenha sido confirmado
    }
}
