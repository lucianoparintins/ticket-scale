package com.ticketscale.interfaces.rest.evento;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record DadosCadastroEvento(
        @NotBlank String nome,
        String descricao,
        @NotNull @Future LocalDateTime dataInicio,
        @NotNull @Future LocalDateTime dataFim
) {}
