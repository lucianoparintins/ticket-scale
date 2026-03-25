package com.ticketscale.infrastructure.security;

import com.ticketscale.domain.usuario.Papel;
import com.ticketscale.domain.usuario.Usuario;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TokenServiceTest {

    private TokenService tokenService;
    private static final String SEGREDO = "12345678123456781234567812345678"; // JJWT 0.12 exige segredo maior para HMAC

    @BeforeEach
    void setUp() {
        tokenService = new TokenService();
        ReflectionTestUtils.setField(tokenService, "segredo", SEGREDO);
    }

    @Test
    @DisplayName("Deve gerar um token válido para um usuário")
    void deveGerarTokenValido() {
        var usuario = new Usuario(UUID.randomUUID(), "usuario_teste", "senha123", Papel.USUARIO);
        
        var token = tokenService.gerarToken(usuario);
        
        assertNotNull(token);
        assertFalse(token.isEmpty());
        
        var login = tokenService.validarToken(token);
        assertEquals("usuario_teste", login);
    }

    @Test
    @DisplayName("Deve validar um token válido e retornar o login")
    void deveValidarTokenValido() {
        var usuario = new Usuario(UUID.randomUUID(), "login_valido", "senha123", Papel.USUARIO);
        var token = tokenService.gerarToken(usuario);
        
        var login = tokenService.validarToken(token);
        
        assertEquals("login_valido", login);
    }

    @Test
    @DisplayName("Deve retornar string vazia ao validar um token inválido")
    void deveRejeitarTokenInvalido() {
        var login = tokenService.validarToken("token_totalmente_invalido");
        
        assertEquals("", login);
    }

    @Test
    @DisplayName("Deve retornar string vazia ao validar um token expirado")
    void deveRejeitarTokenExpirado() {
        // Geramos um token expirado manualmente usando o mesmo segredo
        var key = Keys.hmacShaKeyFor(SEGREDO.getBytes(StandardCharsets.UTF_8));
        var tokenExpirado = Jwts.builder()
                .issuer("ticket-scale-api")
                .subject("usuario_expirado")
                .expiration(Date.from(Instant.now().minus(1, ChronoUnit.HOURS)))
                .signWith(key)
                .compact();
        
        var login = tokenService.validarToken(tokenExpirado);
        
        assertEquals("", login);
    }
}
