package com.ticketscale.interfaces.rest.pagamento;

public record ErroResponseDTO(
    String mensagem,
    String codigo
) {}
