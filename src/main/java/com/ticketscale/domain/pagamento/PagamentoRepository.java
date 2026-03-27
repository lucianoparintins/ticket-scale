package com.ticketscale.domain.pagamento;

import java.util.Optional;
import java.util.UUID;

public interface PagamentoRepository {
    Pagamento salvar(Pagamento pagamento);
    Optional<Pagamento> buscarPorId(UUID id);
    Optional<Pagamento> buscarPorReservaId(UUID reservaId);
    boolean existePagamentoAprovadoParaReserva(UUID reservaId);
}
