package com.ticketscale.interfaces.rest.usuario;

import com.ticketscale.domain.usuario.Papel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DadosCadastroUsuario(
        @NotBlank String login,
        @NotBlank String senha,
        @NotNull Papel papel) {
}
