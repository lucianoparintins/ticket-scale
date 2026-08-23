package com.ticketscale.infrastructure.messaging.listener;

import com.ticketscale.application.usecase.ExpirarReservaUseCase;
import com.ticketscale.domain.event.ReservaCriadaEvent;
import com.ticketscale.infrastructure.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ExpiracaoReservaListener {

    private static final Logger log = LoggerFactory.getLogger(ExpiracaoReservaListener.class);

    private final ExpirarReservaUseCase expirarReservaUseCase;

    public ExpiracaoReservaListener(ExpirarReservaUseCase expirarReservaUseCase) {
        this.expirarReservaUseCase = expirarReservaUseCase;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_RESERVATIONS_EXPIRATION)
    public void processarExpiracao(ReservaCriadaEvent evento) {
        log.info("Iniciando verificação de expiração para a reserva [{}]", evento.getReservaId());
        try {
            UUID reservaId = UUID.fromString(evento.getReservaId());
            expirarReservaUseCase.executar(reservaId);
        } catch (Exception e) {
            log.error("Erro ao processar expiração da reserva [{}]", evento.getReservaId(), e);
            throw e;
        }
    }
}
