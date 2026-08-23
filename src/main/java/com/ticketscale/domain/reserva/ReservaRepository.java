package com.ticketscale.domain.reserva;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, UUID> {

    @Query("SELECT r FROM Reserva r JOIN FETCH r.ingresso i JOIN FETCH i.lote WHERE r.id = :id")
    Optional<Reserva> buscarComIngressoELotePorId(@Param("id") UUID id);

    @Query("SELECT r FROM Reserva r JOIN FETCH r.ingresso i JOIN FETCH i.lote WHERE r.status = com.ticketscale.domain.reserva.StatusReserva.PENDENTE AND r.dataExpiracao < :agora")
    List<Reserva> buscarReservasExpiradas(@Param("agora") LocalDateTime agora);
}
