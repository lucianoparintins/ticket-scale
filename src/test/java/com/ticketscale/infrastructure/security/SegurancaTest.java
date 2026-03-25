package com.ticketscale.infrastructure.security;
import com.ticketscale.application.ports.LockManager;
import com.ticketscale.domain.usuario.Papel;
import com.ticketscale.domain.usuario.Usuario;
import com.ticketscale.infrastructure.persistence.UsuarioJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SegurancaTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private UsuarioJpaRepository repository;

    @MockitoBean
    private LockManager lockManager;

    @Test
    @DisplayName("Deve retornar 403 quando acessar endpoint protegido sem token")
    void deveRetornar403SemToken() throws Exception {
        mockMvc.perform(get("/usuarios/qualquer-id"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve retornar 403 quando o token for inválido")
    void deveRetornar403ComTokenInvalido() throws Exception {
        when(tokenService.validarToken(anyString())).thenReturn("");
        
        mockMvc.perform(get("/usuarios/qualquer-id")
                .header("Authorization", "Bearer token_errado"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve retornar 200 (ou 404) quando o token for válido e usuário existir")
    void deveRetornarSucessoComTokenValido() throws Exception {
        var usuario = new Usuario(UUID.randomUUID(), "usuario_autenticado", "senha", Papel.USUARIO);
        
        when(tokenService.validarToken("token_valido")).thenReturn("usuario_autenticado");
        when(repository.findByLogin("usuario_autenticado")).thenReturn(usuario);
        
        mockMvc.perform(get("/usuarios/" + usuario.getId())
                .header("Authorization", "Bearer token_valido"))
                .andExpect(status().isNotFound());
    }
}
