package com.ticketscale.infrastructure.persistence;

import com.ticketscale.domain.usuario.Usuario;
import com.ticketscale.domain.usuario.UsuarioRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import java.util.UUID;

import java.util.Optional;

@Repository
public interface UsuarioJpaRepository extends JpaRepository<Usuario, UUID>, UsuarioRepository {
    UserDetails findByLogin(String login);

    @Override
    default Usuario salvar(Usuario usuario) {
        return save(usuario);
    }

    @Override
    default Optional<Usuario> buscarPorId(UUID id) {
        return findById(id);
    }
}
