package com.ticketscale.domain.pagamento;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DadosMetodoPagamentoTest {

    @Test
    void dadosPix_DeveCriarComSucesso_QuandoChaveValida() {
        DadosPix pix = new DadosPix("chave-123");
        assertEquals("chave-123", pix.chavePix());
    }

    @Test
    void dadosPix_DeveLancarExcecao_QuandoChaveInvalida() {
        assertThrows(IllegalArgumentException.class, () -> new DadosPix(null));
        assertThrows(IllegalArgumentException.class, () -> new DadosPix(""));
    }

    @Test
    void dadosCartao_DeveCriarComSucesso_QuandoDadosValidos() {
        DadosCartao cartao = new DadosCartao("1234", "Titular", "12/25", "123", 1);
        assertEquals("1234", cartao.numeroCartao());
        assertEquals(1, cartao.parcelas());
    }

    @Test
    void dadosCartao_DeveLancarExcecao_QuandoParcelasInvalida() {
        assertThrows(IllegalArgumentException.class, () -> new DadosCartao("1234", "Titular", "12/25", "123", 0));
    }

    @Test
    void patternMatching_DeveFuncionarComSealedInterface() {
        DadosMetodoPagamento dados = new DadosPix("chave");
        
        String resultado = switch (dados) {
            case DadosPix p -> "pix";
            case DadosCartao c -> "cartao";
        };
        
        assertEquals("pix", resultado);
    }
}
