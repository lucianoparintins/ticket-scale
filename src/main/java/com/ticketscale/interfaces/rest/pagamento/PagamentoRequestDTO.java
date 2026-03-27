package com.ticketscale.interfaces.rest.pagamento;

import com.ticketscale.domain.pagamento.DadosCartao;
import com.ticketscale.domain.pagamento.DadosMetodoPagamento;
import com.ticketscale.domain.pagamento.DadosPix;
import com.ticketscale.domain.pagamento.MetodoPagamento;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record PagamentoRequestDTO(
    @NotNull UUID reservaId,
    @NotNull MetodoPagamento metodoPagamento,
    DadosPixDTO dadosPix,
    DadosCartaoDTO dadosCartao
) {
    public DadosMetodoPagamento toDadosMetodo() {
        return switch (metodoPagamento) {
            case PIX -> {
                if (dadosPix == null) throw new IllegalArgumentException("Dados Pix são obrigatórios.");
                yield new DadosPix(dadosPix.chavePix());
            }
            case CARTAO_DEBITO, CARTAO_CREDITO -> {
                if (dadosCartao == null) throw new IllegalArgumentException("Dados do cartão são obrigatórios.");
                yield new DadosCartao(
                    dadosCartao.numeroCartao(),
                    dadosCartao.nomeTitular(),
                    dadosCartao.validade(),
                    dadosCartao.cvv(),
                    dadosCartao.parcelas()
                );
            }
        };
    }
}
