package com.ticketscale.infrastructure.messaging;

import com.ticketscale.domain.event.ReservaCriadaEvent;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class RabbitMQIntegrationTest {

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitMQEventPublisher publisher;

    @Test
    void integracao_publicarEvento_naoDeveLancarExcecaoQuandoPublicado() {
        ReservaCriadaEvent evento = new ReservaCriadaEvent("res-int", "usr-int", "lote-int");

        assertDoesNotThrow(() -> publisher.publicarReservaCriada(evento));
    }
}
