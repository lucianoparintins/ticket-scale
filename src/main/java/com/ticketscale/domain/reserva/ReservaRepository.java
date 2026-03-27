package com.ticketscale.domain.reserva;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, UUID> {

    @Query("SELECT r FROM Reserva r JOIN FETCH r.ingresso i JOIN FETCH i.lote WHERE r.id = :id")
    Optional<Reserva> buscarComIngressoELotePorId(@Param("id") UUID id);
}
