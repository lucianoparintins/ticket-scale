package com.ticketscale.application.reserva;

import com.ticketscale.application.port.out.EventPublisher;
import com.ticketscale.domain.event.CacheInvalidadoEvent;
import com.ticketscale.domain.reserva.Lote;
import com.ticketscale.domain.reserva.LoteRepository;
import com.ticketscale.infrastructure.config.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class LoteService {

    private final LoteRepository repository;
    private final EventPublisher eventPublisher;

    public LoteService(LoteRepository repository, EventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Cacheable(value = CacheConfig.CACHE_LOTES, key = "#id")
    public Lote buscarPorId(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lote não encontrado."));
    }

    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_LOTES, key = "#id")
    public Lote atualizar(UUID id, String nome, BigDecimal preco, Integer capacidade) {
        Lote lote = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lote não encontrado."));

        // Usando o builder para criar uma nova instância atualizada (mantendo o ID)
        Lote loteAtualizado = Lote.builder()
                .id(lote.getId())
                .evento(lote.getEvento())
                .nome(nome != null ? nome : lote.getNome())
                .preco(preco != null ? preco : lote.getPreco())
                .capacidade(capacidade != null ? capacidade : lote.getCapacidade())
                .build();

        Lote salvo = repository.save(loteAtualizado);
        
        // Notifica outras instâncias sobre a invalidação (via RabbitMQ)
        eventPublisher.publicarInvalidacaoCache(new CacheInvalidadoEvent(CacheConfig.CACHE_LOTES, id.toString()));
        
        return salvo;
    }
}
