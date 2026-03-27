package com.ticketscale.application.usecase;

import com.ticketscale.application.port.out.*;
import com.ticketscale.domain.event.PagamentoConfirmadoEvent;
import com.ticketscale.domain.pagamento.*;
import com.ticketscale.domain.reserva.Reserva;
import com.ticketscale.domain.reserva.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ProcessarPagamentoUseCase {

    private final ReservaRepository reservaRepository;
    private final PagamentoRepository pagamentoRepository;
    private final GatewayPagamentoResolver gatewayResolver;
    private final EventPublisher eventPublisher;
    private final LockManager lockManager;

    public ProcessarPagamentoUseCase(
            ReservaRepository reservaRepository,
            PagamentoRepository pagamentoRepository,
            GatewayPagamentoResolver gatewayResolver,
            EventPublisher eventPublisher,
            LockManager lockManager) {
        this.reservaRepository = reservaRepository;
        this.pagamentoRepository = pagamentoRepository;
        this.gatewayResolver = gatewayResolver;
        this.eventPublisher = eventPublisher;
        this.lockManager = lockManager;
    }

    @Transactional
    public void executar(SolicitacaoPagamento solicitacao) {
        String lockKey = "lock:pagamento:reserva:" + solicitacao.reservaId();
        
        if (!lockManager.acquireLock(lockKey, 10)) {
            throw new PagamentoException("Não foi possível processar o pagamento no momento. Tente novamente.");
        }

        try {
            Reserva reserva = reservaRepository.buscarComIngressoELotePorId(solicitacao.reservaId())
                    .orElseThrow(() -> new ReservaNaoEncontradaException("Reserva não encontrada: " + solicitacao.reservaId()));

            if (reserva.isExpirada()) {
                throw new PagamentoException("Reserva expirada. Não é possível realizar o pagamento.");
            }

            if (pagamentoRepository.existePagamentoAprovadoParaReserva(reserva.getId())) {
                throw new PagamentoDuplicadoException("Já existe um pagamento aprovado para esta reserva.");
            }

            Pagamento pagamento = new Pagamento(
                    reserva.getId(),
                    reserva.getIngresso().getLote().getPreco(),
                    solicitacao.metodoPagamento()
            );

            GatewayPagamento gateway = gatewayResolver.resolver(solicitacao.metodoPagamento());
            ResultadoPagamento resultado = gateway.processarPagamento(solicitacao);

            if (resultado.sucesso()) {
                pagamento.confirmar(resultado.transacaoExternaId());
                reserva.confirmarPagamento();
                
                Pagamento pagamentoSalvo = pagamentoRepository.salvar(pagamento);
                reservaRepository.save(reserva);

                eventPublisher.publicarPagamentoConfirmado(new PagamentoConfirmadoEvent(
                        reserva.getId().toString(),
                        pagamentoSalvo.getId().toString(),
                        pagamentoSalvo.getValor().toString(),
                        pagamentoSalvo.getMetodoPagamento().name()
                ));
            } else {
                pagamento.recusar();
                pagamentoRepository.salvar(pagamento);
                throw new PagamentoRecusadoException("Pagamento recusado: " + resultado.mensagemErro());
            }

        } finally {
            lockManager.releaseLock(lockKey);
        }
    }
}
