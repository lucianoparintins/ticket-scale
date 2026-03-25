package com.ticketscale.infrastructure.persistence;

import com.ticketscale.domain.usuario.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UsuarioJpaRepository extends JpaRepository<Usuario, UUID> {
    UserDetails findByLogin(String login);
}
