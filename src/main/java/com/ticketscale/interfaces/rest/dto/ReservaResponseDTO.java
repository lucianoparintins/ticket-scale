package com.ticketscale.interfaces.rest.dto;

import com.ticketscale.domain.reserva.StatusReserva;
import java.time.LocalDateTime;
import java.util.UUID;

public record ReservaResponseDTO(
    UUID id,
    UUID usuarioId,
    UUID ingressoId,
    StatusReserva status,
    LocalDateTime dataCriacao,
    LocalDateTime dataExpiracao
) {}
