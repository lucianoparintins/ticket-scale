package com.ticketscale.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_TICKETSCALE_EVENTS = "ticketscale.events";
    
    public static final String QUEUE_NOTIFICATIONS = "ticketscale.notifications";
    public static final String QUEUE_RESERVATIONS_EXPIRATION = "ticketscale.reservations.expiration";
    public static final String QUEUE_PAYMENTS_CONFIRMED = "ticketscale.payments.confirmed";
    
    public static final String ROUTING_KEY_RESERVA_CRIADA = "reserva.criada";
    public static final String ROUTING_KEY_RESERVA_EXPIRACAO = "reserva.expiracao";
    public static final String ROUTING_KEY_PAGAMENTO_CONFIRMADO = "pagamento.confirmado";

    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(EXCHANGE_TICKETSCALE_EVENTS);
    }

    @Bean
    public Queue notificationsQueue() {
        return new Queue(QUEUE_NOTIFICATIONS, true);
    }

    @Bean
    public Queue reservationsExpirationQueue() {
        return new Queue(QUEUE_RESERVATIONS_EXPIRATION, true);
    }

    @Bean
    public Queue paymentsConfirmedQueue() {
        return new Queue(QUEUE_PAYMENTS_CONFIRMED, true);
    }

    @Bean
    public Binding notificationsBinding(Queue notificationsQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(notificationsQueue).to(eventsExchange).with(ROUTING_KEY_RESERVA_CRIADA);
    }

    @Bean
    public Binding reservationsExpirationBinding(Queue reservationsExpirationQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(reservationsExpirationQueue).to(eventsExchange).with(ROUTING_KEY_RESERVA_EXPIRACAO);
    }

    @Bean
    public Binding paymentsConfirmedBinding(Queue paymentsConfirmedQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(paymentsConfirmedQueue).to(eventsExchange).with(ROUTING_KEY_PAGAMENTO_CONFIRMADO);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
