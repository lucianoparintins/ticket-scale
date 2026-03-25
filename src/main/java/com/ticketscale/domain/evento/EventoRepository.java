package com.ticketscale.domain.evento;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventoRepository {
    Evento salvar(Evento evento);
    Optional<Evento> buscarPorId(UUID id);
    List<Evento> listarAtivos();
    void remover(UUID id);
}
