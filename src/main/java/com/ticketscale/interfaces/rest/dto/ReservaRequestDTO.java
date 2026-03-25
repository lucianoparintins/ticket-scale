package com.ticketscale.interfaces.rest.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ReservaRequestDTO(
    @NotNull(message = "O ID do lote é obrigatório") UUID loteId,
    @NotNull(message = "O ID do usuário é obrigatório") UUID usuarioId
) {}
