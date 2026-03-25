package com.ticketscale.domain.evento;

import java.time.LocalDateTime;

public record PeriodoEvento(LocalDateTime dataInicio, LocalDateTime dataFim) {
    public PeriodoEvento {
        if (dataInicio == null || dataFim == null) {
            throw new IllegalArgumentException("Datas de início e fim são obrigatórias.");
        }
        if (dataFim.isBefore(dataInicio)) {
            throw new IllegalArgumentException("A data de fim não pode ser antes da data de início.");
        }
    }

    public boolean isNoFuturo() {
        return dataInicio.isAfter(LocalDateTime.now());
    }
}
