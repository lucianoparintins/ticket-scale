package com.ticketscale.infrastructure.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DataSqlPasswordPepperCheckTest {

    // Mirrors src/main/resources/data.sql (admin123 / usuario123)
    private static final String ADMIN_HASH =
            "$argon2id$v=19$m=65536,t=3,p=1$b26kXMjEClgouyQ33uRVUA$KHVkEe76ax/GDEncDTzNt8q2mNpcBAscwUbvbeeU4fw";
    private static final String USUARIO_HASH =
            "$argon2id$v=19$m=65536,t=3,p=1$yIgUuXAj08F+BQHrhHBDuw$agEUzs3oKFsn5gbOD2EosIiXXSWKXu0ABG9gB2jjLSY";

    @Test
    @DisplayName("Identifica qual pepper valida os hashes do data.sql")
    void identificaPepperDoDataSql() {
        var candidates = new LinkedHashSet<String>();

        var envPepper = System.getenv("PASSWORD_PEPPER");
        if (envPepper != null && !envPepper.isBlank()) {
            candidates.add(envPepper);
        }

        // Defaults conhecidos do repo/codigo
        candidates.add("dev-pepper-change-in-prod");
        candidates.add("default_pepper");
        candidates.add(""); // sem pepper

        List<String> matches = new ArrayList<>();

        for (String pepper : candidates) {
            var hasher = new Argon2PasswordHasher();
            ReflectionTestUtils.setField(hasher, "pepper", pepper);

            boolean adminOk = hasher.verify("admin123", ADMIN_HASH);
            boolean usuarioOk = hasher.verify("usuario123", USUARIO_HASH);

            if (adminOk && usuarioOk) {
                matches.add(pepper);
                System.out.println("data.sql pepper MATCH: '" + pepper + "'");
            }
        }

        assertTrue(
                !matches.isEmpty(),
                "Nenhum pepper candidato validou os hashes do data.sql. " +
                        "Ajuste PASSWORD_PEPPER para o valor usado ao gerar os hashes."
        );
    }
}

