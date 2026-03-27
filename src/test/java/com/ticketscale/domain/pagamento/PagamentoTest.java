package com.ticketscale.domain.pagamento;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class PagamentoTest {

    @Test
    void confirmar_DeveMudarStatusParaAprovado_QuandoPendente() {
        Pagamento pagamento = new Pagamento(UUID.randomUUID(), BigDecimal.TEN, MetodoPagamento.PIX);
        String transacaoId = "trans-123";
        
        pagamento.confirmar(transacaoId);
        
        assertEquals(StatusPagamento.APROVADO, pagamento.getStatus());
        assertEquals(transacaoId, pagamento.getTransacaoExternaId());
        assertTrue(pagamento.isAprovado());
    }

    @Test
    void confirmar_DeveLancarExcecao_QuandoNaoPendente() {
        Pagamento pagamento = new Pagamento(UUID.randomUUID(), BigDecimal.TEN, MetodoPagamento.PIX);
        pagamento.confirmar("trans-123");
        
        assertThrows(PagamentoException.class, () -> pagamento.confirmar("trans-456"));
    }

    @Test
    void recusar_DeveMudarStatusParaRecusado_QuandoPendente() {
        Pagamento pagamento = new Pagamento(UUID.randomUUID(), BigDecimal.TEN, MetodoPagamento.PIX);
        
        pagamento.recusar();
        
        assertEquals(StatusPagamento.RECUSADO, pagamento.getStatus());
        assertFalse(pagamento.isAprovado());
    }

    @Test
    void recusar_DeveLancarExcecao_QuandoNaoPendente() {
        Pagamento pagamento = new Pagamento(UUID.randomUUID(), BigDecimal.TEN, MetodoPagamento.PIX);
        pagamento.recusar();
        
        assertThrows(PagamentoException.class, () -> pagamento.recusar());
    }

    @Test
    void construtor_DeveLancarExcecao_QuandoValorInvalido() {
        assertThrows(IllegalArgumentException.class, () -> new Pagamento(UUID.randomUUID(), BigDecimal.ZERO, MetodoPagamento.PIX));
        assertThrows(IllegalArgumentException.class, () -> new Pagamento(UUID.randomUUID(), new BigDecimal("-1"), MetodoPagamento.PIX));
    }
}
