package com.ticketscale.application.usecase;

import com.ticketscale.application.port.out.EventPublisher;
import com.ticketscale.application.port.out.LockManager;
import com.ticketscale.domain.event.ReservaCriadaEvent;
import com.ticketscale.domain.reserva.*;
import com.ticketscale.domain.usuario.Usuario;
import com.ticketscale.domain.usuario.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ReservarIngressoUseCase {

    private final IngressoRepository ingressoRepository;
    private final ReservaRepository reservaRepository;
    private final LoteRepository loteRepository;
    private final UsuarioRepository usuarioRepository;
    private final LockManager lockManager;
    private final EventPublisher eventPublisher;

    public ReservarIngressoUseCase(IngressoRepository ingressoRepository,
                                   ReservaRepository reservaRepository,
                                   LoteRepository loteRepository,
                                   UsuarioRepository usuarioRepository,
                                   LockManager lockManager,
                                   EventPublisher eventPublisher) {
        this.ingressoRepository = ingressoRepository;
        this.reservaRepository = reservaRepository;
        this.loteRepository = loteRepository;
        this.usuarioRepository = usuarioRepository;
        this.lockManager = lockManager;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Reserva executar(UUID loteId, UUID usuarioId) {
        // Conforme aprovado no plano de implementacao, o lock distributed tem validade de 10s
        String lockKey = "lock:reserva:lote:" + loteId;
        boolean locked = lockManager.acquireLock(lockKey, 10);

        if (!locked) {
            throw new RuntimeException("Não foi possível adquirir o lock para este lote. Muitas requisições simultâneas. Tente novamente.");
        }

        try {
            Lote lote = loteRepository.findById(loteId)
                    .orElseThrow(() -> new IllegalArgumentException("Lote não encontrado."));

            Ingresso ingressoLivre = ingressoRepository.findFirstByLoteIdAndStatus(loteId, StatusIngresso.LIVRE)
                    .orElseThrow(() -> new RuntimeException("Ingressos esgotados ou temporariamente indisponíveis para este lote."));

            Usuario usuario = usuarioRepository.buscarPorId(usuarioId)
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

            ingressoLivre.reservar();
            ingressoRepository.save(ingressoLivre);

            Reserva reserva = new Reserva(usuario, ingressoLivre);
            Reserva reservaSalva = reservaRepository.save(reserva);

            ReservaCriadaEvent eventoCriada = new ReservaCriadaEvent(
                    reservaSalva.getId().toString(),
                    usuario.getId().toString(),
                    loteId.toString()
            );

            eventPublisher.publicarReservaCriada(eventoCriada);
            eventPublisher.publicarReservaExpiracao(eventoCriada);

            return reservaSalva;

        } finally {
            lockManager.releaseLock(lockKey);
        }
    }
}
