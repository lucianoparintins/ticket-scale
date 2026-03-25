package com.ticketscale.interfaces.rest.evento;

import com.ticketscale.domain.evento.Evento;
import java.time.LocalDateTime;
import java.util.UUID;

public record DadosDetalhamentoEvento(
        UUID id,
        String nome,
        String descricao,
        LocalDateTime dataInicio,
        LocalDateTime dataFim,
        boolean ativo
) {
    public DadosDetalhamentoEvento(Evento evento) {
        this(evento.getId(), evento.getNome(), evento.getDescricao(),
             evento.getPeriodo().dataInicio(), evento.getPeriodo().dataFim(), evento.isAtivo());
    }
}
