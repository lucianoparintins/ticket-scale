package com.ticketscale.domain.reserva;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IngressoRepository extends JpaRepository<Ingresso, UUID> {
    
    Optional<Ingresso> findFirstByLoteIdAndStatus(UUID loteId, StatusIngresso status);
}
