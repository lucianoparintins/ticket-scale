package com.ticketscale.application.evento;

import com.ticketscale.domain.evento.Evento;
import com.ticketscale.domain.evento.EventoRepository;
import com.ticketscale.domain.evento.PeriodoEvento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class EventoService {

    private final EventoRepository repository;

    public EventoService(EventoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Evento criar(String nome, String descricao, LocalDateTime dataInicio, LocalDateTime dataFim) {
        var periodo = new PeriodoEvento(dataInicio, dataFim);
        var evento = Evento.builder()
                .nome(nome)
                .descricao(descricao)
                .periodo(periodo)
                .build();
        return repository.salvar(evento);
    }

    @Transactional
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
        return repository.salvar(evento);
    }

    public List<Evento> listarAtivos() {
        return repository.listarAtivos();
    }

    public Evento buscarPorId(UUID id) {
        return repository.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Evento não encontrado."));
    }

    @Transactional
    public void desativar(UUID id) {
        var evento = buscarPorId(id);
        evento.desativar();
        repository.salvar(evento);
    }
}
