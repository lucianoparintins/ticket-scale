package com.ticketscale.infrastructure.scheduler;

import com.ticketscale.application.usecase.ExpirarReservaUseCase;
import com.ticketscale.domain.reserva.Reserva;
import com.ticketscale.domain.reserva.ReservaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ExpiracaoReservaScheduler {

    private static final Logger log = LoggerFactory.getLogger(ExpiracaoReservaScheduler.class);

    private final ReservaRepository reservaRepository;
    private final ExpirarReservaUseCase expirarReservaUseCase;

    public ExpiracaoReservaScheduler(ReservaRepository reservaRepository, ExpirarReservaUseCase expirarReservaUseCase) {
        this.reservaRepository = reservaRepository;
        this.expirarReservaUseCase = expirarReservaUseCase;
    }

    @Scheduled(fixedRate = 120_000) // A cada 2 minutos
    public void verificarReservasExpiradas() {
        log.debug("Iniciando verificação periódica de reservas expiradas...");
        try {
            List<Reserva> expiradas = reservaRepository.buscarReservasExpiradas(LocalDateTime.now());
            if (!expiradas.isEmpty()) {
                log.info("Encontradas {} reservas expiradas pendentes para processamento.", expiradas.size());
            }

            for (Reserva reserva : expiradas) {
                try {
                    expirarReservaUseCase.executar(reserva.getId());
                } catch (Exception e) {
                    log.error("Erro ao expirar reserva [{}] pelo scheduler.", reserva.getId(), e);
                }
            }
        } catch (Exception e) {
            log.error("Erro ao buscar reservas expiradas no scheduler.", e);
        }
    }
}
