package com.ticketscale.infrastructure.persistence.pagamento;

import com.ticketscale.domain.pagamento.Pagamento;
import com.ticketscale.domain.pagamento.PagamentoRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PagamentoJpaRepository
    extends JpaRepository<Pagamento, UUID>, PagamentoRepository {

    @Override
    default Pagamento salvar(Pagamento pagamento) { return save(pagamento); }

    @Override
    default Optional<Pagamento> buscarPorId(UUID id) { return findById(id); }

    @Query("SELECT p FROM Pagamento p WHERE p.reservaId = :reservaId")
    Optional<Pagamento> buscarPorReservaId(@Param("reservaId") UUID reservaId);

    @Query("SELECT COUNT(p) > 0 FROM Pagamento p WHERE p.reservaId = :reservaId AND p.status = 'APROVADO'")
    boolean existePagamentoAprovadoParaReserva(@Param("reservaId") UUID reservaId);
}
