package com.ticketscale.interfaces.rest.pagamento;

public record DadosCartaoDTO(
    String numeroCartao,
    String nomeTitular,
    String validade,
    String cvv,
    int parcelas
) {}
