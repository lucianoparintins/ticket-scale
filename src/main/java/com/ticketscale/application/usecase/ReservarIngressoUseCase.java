package com.ticketscale.application.usecase;

import com.ticketscale.application.ports.LockManager;
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

    public ReservarIngressoUseCase(IngressoRepository ingressoRepository,
                                   ReservaRepository reservaRepository,
                                   LoteRepository loteRepository,
                                   UsuarioRepository usuarioRepository,
                                   LockManager lockManager) {
        this.ingressoRepository = ingressoRepository;
        this.reservaRepository = reservaRepository;
        this.loteRepository = loteRepository;
        this.usuarioRepository = usuarioRepository;
        this.lockManager = lockManager;
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
            return reservaRepository.save(reserva);

        } finally {
            lockManager.releaseLock(lockKey);
        }
    }
}
