package com.ticketscale.interfaces.rest.usuario;

import com.ticketscale.domain.usuario.Papel;
import com.ticketscale.domain.usuario.Usuario;

import java.util.UUID;

public record DadosDetalhamentoUsuario(UUID id, String login, Papel papel) {
    public DadosDetalhamentoUsuario(Usuario usuario) {
        this(usuario.getId(), usuario.getLogin(), usuario.getPapel());
    }
}
