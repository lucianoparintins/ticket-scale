package com.ticketscale.interfaces.rest.autenticacao;

import com.ticketscale.domain.usuario.Papel;
import com.ticketscale.domain.usuario.Usuario;
import com.ticketscale.infrastructure.security.SecurityFilter;
import com.ticketscale.infrastructure.security.TokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AutenticacaoController.class)
@AutoConfigureMockMvc(addFilters = false)
class AutenticacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationManager manager;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private SecurityFilter securityFilter;

    @Test
    @DisplayName("Deve retornar 200 com o token quando as credenciais forem válidas")
    void deveRetornar200ComTokenQuandoLoginValido() throws Exception {
        var usuario = new Usuario(UUID.randomUUID(), "login_correto", "senha_correta", Papel.USUARIO);
        var auth = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
        
        when(manager.authenticate(any())).thenReturn(auth);
        when(tokenService.gerarToken(any())).thenReturn("token_jwt_valido");
        
        var json = "{\"login\":\"login_correto\", \"senha\":\"senha_correta\"}";
        
        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token_jwt_valido"));
    }

    @Test
    @DisplayName("Deve retornar 400 quando o JSON for inválido (validation)")
    void deveRetornar400QuandoJsonInvalido() throws Exception {
        var json = "{\"login\":\"\", \"senha\":\"\"}";
        
        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve lançar BadCredentialsException (que resulta em ServletException no MockMvc) quando as credenciais forem inválidas")
    void deveLancarExcecaoQuandoCredenciaisInvalidas() throws Exception {
        when(manager.authenticate(any())).thenThrow(new BadCredentialsException("Credenciais inválidas"));
        
        var json = "{\"login\":\"login_errado\", \"senha\":\"senha_errada\"}";

        // O controller captura AuthenticationException e retorna 401.
        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("Credenciais inválidas"));
    }
}
