package com.ticketscale.domain.pagamento;

public record DadosPix(String chavePix)
    implements DadosMetodoPagamento {
    public DadosPix {
        if (chavePix == null || chavePix.isBlank()) {
            throw new IllegalArgumentException("Chave Pix é obrigatória.");
        }
    }
}
