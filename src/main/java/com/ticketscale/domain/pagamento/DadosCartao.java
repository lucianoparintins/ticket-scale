package com.ticketscale.domain.pagamento;

public record DadosCartao(
    String numeroCartao,
    String nomeTitular,
    String validade,
    String cvv,
    int parcelas
) implements DadosMetodoPagamento {
    public DadosCartao {
        if (numeroCartao == null || numeroCartao.isBlank()) {
            throw new IllegalArgumentException("Número do cartão é obrigatório.");
        }
        if (parcelas < 1) {
            throw new IllegalArgumentException("Parcelas deve ser >= 1.");
        }
    }
}
