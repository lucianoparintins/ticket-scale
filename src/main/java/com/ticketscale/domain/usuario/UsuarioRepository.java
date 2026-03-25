package com.ticketscale.domain.usuario;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository {
    UserDetails findByLogin(String login);
    Usuario salvar(Usuario usuario);
    Optional<Usuario> buscarPorId(UUID id);
}
