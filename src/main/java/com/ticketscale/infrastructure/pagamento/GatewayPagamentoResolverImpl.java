package com.ticketscale.infrastructure.pagamento;

import com.ticketscale.application.port.out.GatewayPagamento;
import com.ticketscale.application.port.out.GatewayPagamentoResolver;
import com.ticketscale.domain.pagamento.MetodoPagamento;
import com.ticketscale.domain.pagamento.MetodoNaoSuportadoException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class GatewayPagamentoResolverImpl implements GatewayPagamentoResolver {
    private final Map<MetodoPagamento, GatewayPagamento> gateways;

    public GatewayPagamentoResolverImpl(List<GatewayPagamento> gatewayList) {
        this.gateways = gatewayList.stream()
            .collect(Collectors.toMap(GatewayPagamento::getMetodoSuportado, g -> g));
    }

    @Override
    public GatewayPagamento resolver(MetodoPagamento metodo) {
        return Optional.ofNullable(gateways.get(metodo))
            .orElseThrow(() -> new MetodoNaoSuportadoException(
                "Método de pagamento não suportado: " + metodo));
    }
}
