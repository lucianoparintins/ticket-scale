package com.ticketscale.application.evento;

import com.ticketscale.application.port.out.EventPublisher;
import com.ticketscale.domain.event.CacheInvalidadoEvent;
import com.ticketscale.domain.evento.Evento;
import com.ticketscale.domain.evento.EventoRepository;
import com.ticketscale.domain.evento.PeriodoEvento;
import com.ticketscale.infrastructure.config.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@org.springframework.cache.annotation.CacheConfig(cacheNames = CacheConfig.CACHE_EVENTOS)
public class EventoService {

    private final EventoRepository repository;
    private final EventPublisher eventPublisher;

    public EventoService(EventoRepository repository, EventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    @CacheEvict(allEntries = true)
    public Evento criar(String nome, String descricao, LocalDateTime dataInicio, LocalDateTime dataFim) {
        var periodo = new PeriodoEvento(dataInicio, dataFim);
        var evento = Evento.builder()
                .nome(nome)
                .descricao(descricao)
                .periodo(periodo)
                .build();
        
        Evento salvo = repository.salvar(evento);
        
        // Notifica outras instâncias sobre a invalidação geral da lista
        eventPublisher.publicarInvalidacaoCache(new CacheInvalidadoEvent(CacheConfig.CACHE_EVENTOS, null));
        
        return salvo;
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(key = "#id"),
        @CacheEvict(allEntries = true)
    })
    public Evento atualizar(UUID id, String nome, String descricao, LocalDateTime dataInicio, LocalDateTime dataFim) {
        var evento = repository.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Evento não encontrado."));
        
        PeriodoEvento novoPeriodo = null;
        if (dataInicio != null || dataFim != null) {
            LocalDateTime inicio = dataInicio != null ? dataInicio : evento.getPeriodo().dataInicio();
            LocalDateTime fim = dataFim != null ? dataFim : evento.getPeriodo().dataFim();
            novoPeriodo = new PeriodoEvento(inicio, fim);
        }
        
        evento.atualizar(nome, descricao, novoPeriodo);
        Evento salvo = repository.salvar(evento);

        // Notifica outras instâncias
        eventPublisher.publicarInvalidacaoCache(new CacheInvalidadoEvent(CacheConfig.CACHE_EVENTOS, id.toString()));
        eventPublisher.publicarInvalidacaoCache(new CacheInvalidadoEvent(CacheConfig.CACHE_EVENTOS, "ativos"));

        return salvo;
    }

    @Cacheable(key = "'ativos'")
    public List<Evento> listarAtivos() {
        return repository.listarAtivos();
    }

    @Cacheable(key = "#id")
    public Evento buscarPorId(UUID id) {
        return repository.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Evento não encontrado."));
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(key = "#id"),
        @CacheEvict(allEntries = true)
    })
    public void desativar(UUID id) {
        var evento = buscarPorId(id);
        evento.desativar();
        repository.salvar(evento);

        // Notifica outras instâncias
        eventPublisher.publicarInvalidacaoCache(new CacheInvalidadoEvent(CacheConfig.CACHE_EVENTOS, id.toString()));
        eventPublisher.publicarInvalidacaoCache(new CacheInvalidadoEvent(CacheConfig.CACHE_EVENTOS, "ativos"));
    }
}
