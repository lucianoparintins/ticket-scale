package com.ticketscale.infrastructure.messaging.listener;

import com.ticketscale.domain.event.ReservaCriadaEvent;
import com.ticketscale.infrastructure.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificacaoListener {

    private static final Logger log = LoggerFactory.getLogger(NotificacaoListener.class);

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NOTIFICATIONS)
    public void processarNotificacao(ReservaCriadaEvent evento) {
        log.info("Processando notificação para o usuário [{}] referente à reserva [{}] do lote [{}]",
                evento.getUsuarioId(), evento.getReservaId(), evento.getLoteId());
        // Lógica de envio de e-mail/SMS entraria aqui
    }
}
