package com.ticketscale.infrastructure.pagamento.mock;

import com.ticketscale.application.port.out.ResultadoPagamento;
import com.ticketscale.application.port.out.SolicitacaoPagamento;
import com.ticketscale.domain.pagamento.DadosPix;
import com.ticketscale.domain.pagamento.MetodoPagamento;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MockGatewayPixTest {

    private final MockGatewayPix gateway = new MockGatewayPix();

    @Test
    void processarPagamento_DeveRetornarSucesso_QuandoDadosValidos() {
        SolicitacaoPagamento solicitacao = new SolicitacaoPagamento(UUID.randomUUID(), BigDecimal.TEN, MetodoPagamento.PIX, new DadosPix("chave"));
        
        ResultadoPagamento resultado = gateway.processarPagamento(solicitacao);
        
        assertTrue(resultado.sucesso());
        assertNotNull(resultado.transacaoExternaId());
        assertTrue(resultado.transacaoExternaId().startsWith("PIX-"));
    }

    @Test
    void processarPagamento_DeveRetornarFalha_QuandoReservaContemFalha() {
        UUID idComFalha = UUID.fromString("00000000-0000-0000-0000-0000deadbeef");
        SolicitacaoPagamento solicitacao = new SolicitacaoPagamento(idComFalha, BigDecimal.TEN, MetodoPagamento.PIX, new DadosPix("chave"));
        
        ResultadoPagamento resultado = gateway.processarPagamento(solicitacao);
        
        assertFalse(resultado.sucesso());
        assertEquals("Falha simulada no pagamento Pix.", resultado.mensagemErro());
    }

    @Test
    void getMetodoSuportado_DeveRetornarPix() {
        assertEquals(MetodoPagamento.PIX, gateway.getMetodoSuportado());
    }
}
