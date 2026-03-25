package com.ticketscale.infrastructure.security;

import com.ticketscale.domain.usuario.PasswordHasher;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class Argon2PasswordHasher implements PasswordHasher, PasswordEncoder {

    private final Argon2 argon2;
    private final int iterations;
    private final int memory;
    private final int parallelism;

    @Value("${PASSWORD_PEPPER:default_pepper}")
    private String pepper;

    public Argon2PasswordHasher() {
        this.argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
        this.iterations = 3;
        this.memory = 65536; // 64MB
        this.parallelism = 1;
    }

    // Para permitir customização via env se necessário no futuro
    public Argon2PasswordHasher(int iterations, int memory, int parallelism) {
        this.argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
        this.iterations = iterations;
        this.memory = memory;
        this.parallelism = parallelism;
    }

    @Override
    public String hash(String rawPassword) {
        char[] passwordWithPepper = (rawPassword + pepper).toCharArray();
        try {
            return argon2.hash(iterations, memory, parallelism, passwordWithPepper);
        } finally {
            argon2.wipeArray(passwordWithPepper);
        }
    }

    @Override
    public boolean verify(String rawPassword, String hashedPassword) {
        char[] passwordWithPepper = (rawPassword + pepper).toCharArray();
        try {
            return argon2.verify(hashedPassword, passwordWithPepper);
        } finally {
            argon2.wipeArray(passwordWithPepper);
        }
    }

    // Métodos do PasswordEncoder para integração com Spring Security
    @Override
    public String encode(CharSequence rawPassword) {
        return hash(rawPassword.toString());
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return verify(rawPassword.toString(), encodedPassword);
    }
}
