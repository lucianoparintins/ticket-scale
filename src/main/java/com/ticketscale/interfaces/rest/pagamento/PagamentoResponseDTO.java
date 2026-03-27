package com.ticketscale.interfaces.rest.pagamento;

import com.ticketscale.domain.pagamento.MetodoPagamento;
import com.ticketscale.domain.pagamento.StatusPagamento;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PagamentoResponseDTO(
    UUID pagamentoId,
    UUID reservaId,
    BigDecimal valor,
    StatusPagamento status,
    MetodoPagamento metodoPagamento,
    String transacaoExternaId,
    LocalDateTime dataCriacao
) {}
