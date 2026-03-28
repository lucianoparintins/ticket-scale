package com.ticketscale.domain.dashboard;

import java.time.LocalDateTime;
import java.util.UUID;

public record FiltroDashboard(
    LocalDateTime dataInicio,
    LocalDateTime dataFim,
    UUID eventoId,
    int pagina,
    int tamanho
) {
    public FiltroDashboard {
        if (pagina < 0) {
            throw new IllegalArgumentException("Página não pode ser negativa.");
        }
        if (tamanho <= 0) {
            throw new IllegalArgumentException("Tamanho deve ser maior que zero.");
        }
        if (dataInicio != null && dataFim != null && dataFim.isBefore(dataInicio)) {
            throw new IllegalArgumentException("Data fim não pode ser anterior à data início.");
        }
    }
}
