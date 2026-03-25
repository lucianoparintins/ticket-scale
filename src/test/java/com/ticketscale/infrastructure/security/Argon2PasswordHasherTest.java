package com.ticketscale.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class Argon2PasswordHasherTest {

    private Argon2PasswordHasher hasher;

    @BeforeEach
    void setUp() {
        hasher = new Argon2PasswordHasher();
        ReflectionTestUtils.setField(hasher, "pepper", "test-pepper");
    }

    @Test
    @DisplayName("Deve gerar hash diferente da senha original")
    void deveGerarHashDiferenteDaSenhaOriginal() {
        String senha = "senha123";
        String hash = hasher.hash(senha);

        assertNotEquals(senha, hash);
        assertTrue(hash.startsWith("$argon2id$"));
    }

    @Test
    @DisplayName("Mesma senha deve gerar hashes diferentes (salt automático)")
    void mesmaSenhaGeraHashesDiferentes() {
        String senha = "senha123";
        String hash1 = hasher.hash(senha);
        String hash2 = hasher.hash(senha);

        assertNotEquals(hash1, hash2);
    }

    @Test
    @DisplayName("Verify deve retornar true para senha correta")
    void verifyRetornaTrueParaSenhaCorreta() {
        String senha = "minhaSenhaSegura";
        String hash = hasher.hash(senha);

        assertTrue(hasher.verify(senha, hash));
    }

    @Test
    @DisplayName("Verify deve retornar false para senha incorreta")
    void verifyRetornaFalseParaSenhaIncorreta() {
        String senhaCorreta = "senhaCorreta";
        String senhaIncorreta = "senhaIncorreta";
        String hash = hasher.hash(senhaCorreta);

        assertFalse(hasher.verify(senhaIncorreta, hash));
    }

    @Test
    @DisplayName("Deve falhar na verificação se o pepper for diferente")
    void deveFalharSePepperDiferente() {
        String senha = "senha123";
        String hash = hasher.hash(senha);

        // Altera o pepper e tenta verificar
        ReflectionTestUtils.setField(hasher, "pepper", "outro-pepper");
        assertFalse(hasher.verify(senha, hash));
    }
}
