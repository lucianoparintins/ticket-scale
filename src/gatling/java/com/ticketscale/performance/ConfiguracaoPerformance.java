package com.ticketscale.performance;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.OpenInjectionStep;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.atOnceUsers;
import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.core.CoreDsl.rampUsers;
import static io.gatling.javaapi.core.CoreDsl.rampUsersPerSec;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

/**
 * Utilitário com configurações padronizadas de performance, protocolos HTTP e cenários comuns.
 */
public final class ConfiguracaoPerformance {

    private ConfiguracaoPerformance() {
        // Construtor privado para classe utilitária
    }

    public static String obterBaseUrl() {
        return System.getProperty("baseUrl", "http://localhost:8080");
    }

    public static int obterUsuarios() {
        return Integer.getInteger("users", 50);
    }

    public static Duration obterTempoRampUp() {
        long segundos = Long.getLong("rampDuration", 10L);
        return Duration.ofSeconds(segundos);
    }

    public static Duration obterTempoDuracao() {
        long segundos = Long.getLong("testDuration", 60L);
        return Duration.ofSeconds(segundos);
    }

    public static String obterPerfil() {
        return System.getProperty("profile", "load").toLowerCase();
    }

    public static HttpProtocolBuilder obterHttpProtocol() {
        return http
                .baseUrl(obterBaseUrl())
                .acceptHeader("application/json")
                .contentTypeHeader("application/json")
                .userAgentHeader("TicketScale-Gatling-PerformanceTest/1.0");
    }

    public static Map<String, String> cabecalhoAutenticado() {
        return Collections.singletonMap("Authorization", "Bearer #{tokenJwt}");
    }

    /**
     * Passo reutilizável para efetuar login e capturar o token JWT na sessão do Gatling.
     */
    public static ChainBuilder autenticar() {
        return exec(
                http("Autenticação - Login")
                        .post("/api/login")
                        .body(StringBody("{\"login\":\"#{login}\",\"senha\":\"#{senha}\"}"))
                        .check(status().is(200))
                        .check(jsonPath("$.token").saveAs("tokenJwt"))
        );
    }

    /**
     * Define o perfil de injeção de usuários baseado na propriedade 'profile'.
     */
    public static List<OpenInjectionStep> obterInjecaoPadrao() {
        int usuarios = obterUsuarios();
        Duration rampUp = obterTempoRampUp();
        Duration duracao = obterTempoDuracao();
        String perfil = obterPerfil();

        return switch (perfil) {
            case "smoke" -> List.of(
                    atOnceUsers(Math.min(usuarios, 5))
            );
            case "stress" -> List.of(
                    rampUsers(usuarios).during(rampUp),
                    constantUsersPerSec(usuarios).during(duracao),
                    rampUsersPerSec(usuarios).to(usuarios * 3.0).during(Duration.ofSeconds(30))
            );
            case "spike" -> List.of(
                    atOnceUsers(usuarios * 2),
                    constantUsersPerSec(usuarios).during(duracao)
            );
            default -> List.of( // "load"
                    rampUsers(usuarios).during(rampUp),
                    constantUsersPerSec(usuarios).during(duracao)
            );
        };
    }
}
