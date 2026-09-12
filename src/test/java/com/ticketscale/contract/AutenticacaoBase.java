package com.ticketscale.contract;

import com.ticketscale.domain.usuario.Papel;
import com.ticketscale.domain.usuario.Usuario;
import com.ticketscale.infrastructure.security.TokenService;
import com.ticketscale.interfaces.rest.autenticacao.AutenticacaoController;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Classe base para os testes de contrato gerados para o endpoint de autenticação.
 */
public abstract class AutenticacaoBase {

    static {
        SpringCompatibilityPatcher.apply();
    }

    @BeforeEach
    void setup() {
        AuthenticationManager manager = Mockito.mock(AuthenticationManager.class);
        TokenService tokenService = Mockito.mock(TokenService.class);

        var usuario = new Usuario(UUID.randomUUID(), "usuario.teste", "senhaValida123", Papel.USUARIO);
        Authentication authSucesso = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());

        when(manager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenAnswer(invocation -> {
            UsernamePasswordAuthenticationToken token = invocation.getArgument(0);
            if ("senhaValida123".equals(token.getCredentials().toString())) {
                return authSucesso;
            }
            throw new BadCredentialsException("Bad credentials");
        });

        when(tokenService.gerarToken(any())).thenReturn("token_jwt_valido");

        AutenticacaoController controller = new AutenticacaoController(manager, tokenService);
        RestAssuredMockMvc.standaloneSetup(controller);
    }
}
