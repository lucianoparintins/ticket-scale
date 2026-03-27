package com.ticketscale.infrastructure.pagamento.mock;

import com.ticketscale.application.port.out.GatewayPagamento;
import com.ticketscale.application.port.out.ResultadoPagamento;
import com.ticketscale.application.port.out.SolicitacaoPagamento;
import com.ticketscale.domain.pagamento.DadosCartao;
import com.ticketscale.domain.pagamento.MetodoPagamento;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Profile({"dev", "test"})
public class MockGatewayCartaoCredito implements GatewayPagamento {

    @Override
    public ResultadoPagamento processarPagamento(SolicitacaoPagamento solicitacao) {
        if (!(solicitacao.dadosMetodo() instanceof DadosCartao)) {
            return new ResultadoPagamento(false, null, "Dados para cartão de crédito inválidos.");
        }

        if (solicitacao.reservaId().toString().contains("deadbeef")) {
            return new ResultadoPagamento(false, null, "Falha simulada no cartão de crédito.");
        }

        return new ResultadoPagamento(true, "CRE-" + UUID.randomUUID(), null);
    }

    @Override
    public MetodoPagamento getMetodoSuportado() {
        return MetodoPagamento.CARTAO_CREDITO;
    }
}
