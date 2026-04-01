package com.ticketscale.infrastructure.messaging.listener;

import com.ticketscale.domain.event.CacheInvalidadoEvent;
import com.ticketscale.infrastructure.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Component
public class CacheInvalidationListener {

    private static final Logger log = LoggerFactory.getLogger(CacheInvalidationListener.class);
    private final CacheManager cacheManager;

    public CacheInvalidationListener(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_CACHE_INVALIDATION)
    public void processarInvalidacao(CacheInvalidadoEvent evento) {
        log.info("Recebido evento de invalidação de cache: [{}] para chave [{}]", 
                evento.cacheName(), evento.key());
        
        var cache = cacheManager.getCache(evento.cacheName());
        if (cache != null) {
            if (evento.key() != null && !evento.key().isBlank()) {
                cache.evict(evento.key());
            } else {
                cache.clear();
            }
        }
    }
}
