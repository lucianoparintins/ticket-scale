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
            SecretKey key = Keys.hmacShaKeyFor(segredo.getBytes(StandardCharsets.UTF_8));
            return Jwts.builder()
                    .issuer("ticket-scale-api")
                    .subject(usuario.getLogin())
                    .claim("id", usuario.getId().toString())
                    .expiration(dataExpiracao())
                    .signWith(key)
                    .compact();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar token jwt", e);
        }
    }

    public String validarToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(segredo.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getSubject();
        } catch (Exception e) {
            return "";
        }
    }

    private Date dataExpiracao() {
        return Date.from(LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00")));
    }
}
