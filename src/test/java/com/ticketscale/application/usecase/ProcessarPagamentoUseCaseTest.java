package com.ticketscale.application.usecase;

import com.ticketscale.application.port.out.*;
import com.ticketscale.application.port.out.LockManager;
import com.ticketscale.domain.event.PagamentoConfirmadoEvent;
import com.ticketscale.domain.pagamento.*;
import com.ticketscale.domain.reserva.Ingresso;
import com.ticketscale.domain.reserva.Lote;
import com.ticketscale.domain.reserva.Reserva;
import com.ticketscale.domain.reserva.ReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessarPagamentoUseCaseTest {

    @Mock private ReservaRepository reservaRepository;
    @Mock private PagamentoRepository pagamentoRepository;
    @Mock private GatewayPagamentoResolver gatewayResolver;
    @Mock private EventPublisher eventPublisher;
    @Mock private LockManager lockManager;

    @InjectMocks private ProcessarPagamentoUseCase useCase;

    private UUID reservaId;
    private Reserva reserva;
    private SolicitacaoPagamento solicitacao;
    private GatewayPagamento gateway;

    @BeforeEach
    void setup() {
        reservaId = UUID.randomUUID();
        Lote lote = Lote.builder()
                .id(UUID.randomUUID())
                .preco(BigDecimal.TEN)
                .evento(mock(com.ticketscale.domain.evento.Evento.class))
                .nome("Lote 1")
                .capacidade(100)
                .build();
        Ingresso ingresso = Ingresso.builder()
                .id(UUID.randomUUID())
                .lote(lote)
                .build();
        ingresso.reservar();
        reserva = Reserva.builder()
                .id(reservaId)
                .usuario(mock(com.ticketscale.domain.usuario.Usuario.class))
                .ingresso(ingresso)
                .status(com.ticketscale.domain.reserva.StatusReserva.PENDENTE)
                .build();
        
        solicitacao = new SolicitacaoPagamento(reservaId, BigDecimal.TEN, MetodoPagamento.PIX, new DadosPix("chave"));
        gateway = mock(GatewayPagamento.class);
    }

    @Test
    void executar_DeveProcessarComSucesso_QuandoTudoValido() {
        when(lockManager.acquireLock(anyString(), anyLong())).thenReturn(true);
        when(reservaRepository.buscarComIngressoELotePorId(reservaId)).thenReturn(Optional.of(reserva));
        when(pagamentoRepository.existePagamentoAprovadoParaReserva(any())).thenReturn(false);
        when(gatewayResolver.resolver(MetodoPagamento.PIX)).thenReturn(gateway);
        when(gateway.processarPagamento(any())).thenReturn(new ResultadoPagamento(true, "trans-123", null));
        when(pagamentoRepository.salvar(any(Pagamento.class))).thenAnswer(invocation -> {
            Pagamento p = invocation.getArgument(0);
            return Pagamento.builder()
                    .id(UUID.randomUUID())
                    .reservaId(p.getReservaId())
                    .valor(p.getValor())
                    .metodoPagamento(p.getMetodoPagamento())
                    .status(p.getStatus())
                    .transacaoExternaId(p.getTransacaoExternaId())
                    .build();
        });

        useCase.executar(solicitacao);

        verify(pagamentoRepository).salvar(any(Pagamento.class));
        verify(reservaRepository).save(reserva);
        verify(eventPublisher).publicarPagamentoConfirmado(any(PagamentoConfirmadoEvent.class));
        verify(lockManager).releaseLock(anyString());
    }

    @Test
    void executar_DeveLancarExcecao_QuandoLockNaoAdquirido() {
        when(lockManager.acquireLock(anyString(), anyLong())).thenReturn(false);

        assertThrows(PagamentoException.class, () -> useCase.executar(solicitacao));
        
        verify(reservaRepository, never()).buscarComIngressoELotePorId(any());
    }

    @Test
    void executar_DeveLancarExcecao_QuandoPagamentoDuplicado() {
        when(lockManager.acquireLock(anyString(), anyLong())).thenReturn(true);
        when(reservaRepository.buscarComIngressoELotePorId(reservaId)).thenReturn(Optional.of(reserva));
        when(pagamentoRepository.existePagamentoAprovadoParaReserva(any())).thenReturn(true);

        assertThrows(PagamentoDuplicadoException.class, () -> useCase.executar(solicitacao));
        
        verify(lockManager).releaseLock(anyString());
    }

    @Test
    void executar_DeveRecusarPagamento_QuandoGatewayFalhar() {
        when(lockManager.acquireLock(anyString(), anyLong())).thenReturn(true);
        when(reservaRepository.buscarComIngressoELotePorId(reservaId)).thenReturn(Optional.of(reserva));
        when(pagamentoRepository.existePagamentoAprovadoParaReserva(any())).thenReturn(false);
        when(gatewayResolver.resolver(MetodoPagamento.PIX)).thenReturn(gateway);
        when(gateway.processarPagamento(any())).thenReturn(new ResultadoPagamento(false, null, "Saldo insuficiente"));

        assertThrows(PagamentoRecusadoException.class, () -> useCase.executar(solicitacao));

        verify(pagamentoRepository).salvar(argThat(p -> p.getStatus() == StatusPagamento.RECUSADO));
        verify(lockManager).releaseLock(anyString());
    }
}
