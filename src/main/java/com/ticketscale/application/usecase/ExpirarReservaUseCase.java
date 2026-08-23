package com.ticketscale.application.usecase;

import com.ticketscale.application.port.out.EventPublisher;
import com.ticketscale.application.port.out.LockManager;
import com.ticketscale.domain.event.CacheInvalidadoEvent;
import com.ticketscale.domain.event.ReservaExpiradaEvent;
import com.ticketscale.domain.reserva.Reserva;
import com.ticketscale.domain.reserva.ReservaRepository;
import com.ticketscale.domain.reserva.StatusReserva;
import com.ticketscale.infrastructure.config.CacheConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ExpirarReservaUseCase {

    private static final Logger log = LoggerFactory.getLogger(ExpirarReservaUseCase.class);

    private final ReservaRepository reservaRepository;
    private final LockManager lockManager;
    private final EventPublisher eventPublisher;

    public ExpirarReservaUseCase(
            ReservaRepository reservaRepository,
            LockManager lockManager,
            EventPublisher eventPublisher) {
        this.reservaRepository = reservaRepository;
        this.lockManager = lockManager;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void executar(UUID reservaId) {
        String lockKey = "lock:pagamento:reserva:" + reservaId;

        if (!lockManager.acquireLock(lockKey, 10)) {
            log.warn("Não foi possível adquirir lock para expirar reserva: {}", reservaId);
            throw new RuntimeException("Não foi possível adquirir o lock para expirar a reserva. Tente novamente.");
        }

        try {
            Reserva reserva = reservaRepository.buscarComIngressoELotePorId(reservaId).orElse(null);

            if (reserva == null) {
                log.warn("Reserva [{}] não encontrada para expiração.", reservaId);
                return;
            }

            if (reserva.getStatus() != StatusReserva.PENDENTE) {
                log.info("Reserva [{}] não está mais PENDENTE (status atual: {}). Expiração ignorada.",
                        reservaId, reserva.getStatus());
                return;
            }

            reserva.cancelar();
            reservaRepository.save(reserva);

            log.info("Reserva [{}] expirada com sucesso. Ingresso [{}] liberado.",
                    reservaId, reserva.getIngresso().getId());

            eventPublisher.publicarReservaExpirada(new ReservaExpiradaEvent(
                    reserva.getId().toString(),
                    reserva.getIngresso().getId().toString(),
                    reserva.getIngresso().getLote().getId().toString(),
                    reserva.getUsuario().getId().toString()
            ));

            if (reserva.getIngresso().getLote().getEvento() != null) {
                String eventoId = reserva.getIngresso().getLote().getEvento().getId().toString();
                eventPublisher.publicarInvalidacaoCache(new CacheInvalidadoEvent(CacheConfig.CACHE_EVENTOS, eventoId));
            }

        } finally {
            lockManager.releaseLock(lockKey);
        }
    }
}
