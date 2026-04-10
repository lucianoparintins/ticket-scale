package com.ticketscale.infrastructure.security;

import com.ticketscale.domain.usuario.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String segredo;

    public String gerarToken(Usuario usuario) {
        try {
            return Jwts.builder()
                    .issuer("ticket-scale-api")
                    .subject(usuario.getLogin())
                    .claim("id", usuario.getId().toString())
                    .expiration(dataExpiracao())
                    .signWith(secretKey())
                    .compact();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar token jwt", e);
        }
    }

    public String validarToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getSubject();
        } catch (Exception e) {
            return "";
        }
    }

    private SecretKey secretKey() {
        if (segredo == null || segredo.isBlank()) {
            throw new IllegalStateException("JWT secret nao configurado. Defina JWT_SECRET (minimo 32 bytes).");
        }

        byte[] keyBytes = segredo.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET muito curto (" + (keyBytes.length * 8) + " bits). " +
                            "Use no minimo 256 bits (32 bytes) para HS256+. " +
                            "Ex: JWT_SECRET='uma-chave-com-32-bytes-ou-mais...'"
            );
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Date dataExpiracao() {
        return Date.from(LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00")));
    }
}
