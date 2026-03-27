package com.ticketscale.infrastructure.pagamento;

import com.ticketscale.application.port.out.GatewayPagamento;
import com.ticketscale.domain.pagamento.MetodoPagamento;
import com.ticketscale.domain.pagamento.MetodoNaoSuportadoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GatewayPagamentoResolverImplTest {

    private GatewayPagamento pixGateway;
    private GatewayPagamento cartaoGateway;
    private GatewayPagamentoResolverImpl resolver;

    @BeforeEach
    void setup() {
        pixGateway = mock(GatewayPagamento.class);
        when(pixGateway.getMetodoSuportado()).thenReturn(MetodoPagamento.PIX);
        
        cartaoGateway = mock(GatewayPagamento.class);
        when(cartaoGateway.getMetodoSuportado()).thenReturn(MetodoPagamento.CARTAO_CREDITO);

        resolver = new GatewayPagamentoResolverImpl(List.of(pixGateway, cartaoGateway));
    }

    @Test
    void resolver_DeveRetornarGatewayCorreto() {
        assertEquals(pixGateway, resolver.resolver(MetodoPagamento.PIX));
        assertEquals(cartaoGateway, resolver.resolver(MetodoPagamento.CARTAO_CREDITO));
    }

    @Test
    void resolver_DeveLancarExcecao_QuandoMetodoNaoSuportado() {
        assertThrows(MetodoNaoSuportadoException.class, () -> resolver.resolver(MetodoPagamento.CARTAO_DEBITO));
    }
}
