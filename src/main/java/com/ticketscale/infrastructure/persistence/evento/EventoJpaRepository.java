package com.ticketscale.infrastructure.persistence.evento;

import com.ticketscale.domain.evento.Evento;
import com.ticketscale.domain.evento.EventoRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventoJpaRepository extends JpaRepository<Evento, UUID>, EventoRepository {
    
    @Override
    default Evento salvar(Evento evento) {
        return save(evento);
    }

    @Override
    default Optional<Evento> buscarPorId(UUID id) {
        return findById(id);
    }

    @Override
    default List<Evento> listarAtivos() {
        return findAll().stream().filter(Evento::isAtivo).toList();
    }

    @Override
    default void remover(UUID id) {
        deleteById(id);
    }
}
