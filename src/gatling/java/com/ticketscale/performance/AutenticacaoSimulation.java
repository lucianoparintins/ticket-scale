package com.ticketscale.performance;

import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;

import static io.gatling.javaapi.core.CoreDsl.csv;
import static io.gatling.javaapi.core.CoreDsl.global;
import static io.gatling.javaapi.core.CoreDsl.scenario;

/**
 * Simulação de carga e estresse para o endpoint de Autenticação / Login.
 * Avalia o throughput do hashing Argon2id e emissão de tokens JWT.
 */
public class AutenticacaoSimulation extends Simulation {

    private final FeederBuilder<String> alimentadorUsuarios = csv("data/usuarios.csv").circular();

    private final ScenarioBuilder cenarioAutenticacao = scenario("Cenário de Autenticação")
            .feed(alimentadorUsuarios)
            .exec(ConfiguracaoPerformance.autenticar());

    public AutenticacaoSimulation() {
        setUp(
                cenarioAutenticacao.injectOpen(ConfiguracaoPerformance.obterInjecaoPadrao())
        )
        .protocols(ConfiguracaoPerformance.obterHttpProtocol())
        .assertions(
                global().successfulRequests().percent().gte(99.0),
                global().responseTime().percentile3().lt(500),  // p95 < 500ms
                global().responseTime().percentile4().lt(1000) // p99 < 1000ms
        );
    }
}
