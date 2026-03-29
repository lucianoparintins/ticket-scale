package com.ticketscale.application.usuario;

import com.ticketscale.domain.usuario.Papel;
import com.ticketscale.domain.usuario.Usuario;
import com.ticketscale.infrastructure.persistence.UsuarioJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AutenticacaoServiceTest {

    @Mock
    private UsuarioJpaRepository repository;

    @InjectMocks
    private AutenticacaoService service;

    @Test
    @DisplayName("Deve retornar UserDetails quando o usuário for encontrado")
    void deveRetornarUserDetailsQuandoEncontrado() {
        var usuario = new Usuario(UUID.randomUUID(), "usuario_existente", "senha", Papel.USUARIO);
        when(repository.findByLogin("usuario_existente")).thenReturn(usuario);
        
        UserDetails userDetails = service.loadUserByUsername("usuario_existente");
        
        assertNotNull(userDetails);
        assertEquals("usuario_existente", userDetails.getUsername());
    }

    @Test
    @DisplayName("Deve retornar null quando o usuário não for encontrado")
    void deveRetornarNullQuandoNaoEncontrado() {
        // O AutenticacaoService atual não lança UsernameNotFoundException explicitamente
        // Ele retorna o resultado do repository, que pode ser null.
        when(repository.findByLogin("usuario_inexistente")).thenReturn(null);
        
        UserDetails userDetails = service.loadUserByUsername("usuario_inexistente");
        
        assertNull(userDetails);
    }
}
