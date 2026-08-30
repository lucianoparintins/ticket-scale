package com.ticketscale.performance;

import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;

import static io.gatling.javaapi.core.CoreDsl.csv;
import static io.gatling.javaapi.core.CoreDsl.doIf;
import static io.gatling.javaapi.core.CoreDsl.global;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

/**
 * Simulação de performance para consulta e listagem de eventos.
 * Avalia o impacto e eficiência do cache distribuído Redis.
 */
public class ConsultaEventosSimulation extends Simulation {

    private final FeederBuilder<String> alimentadorUsuarios = csv("data/usuarios.csv").circular();

    private final ScenarioBuilder cenarioConsultaEventos = scenario("Cenário de Consulta de Eventos (Cache)")
            .feed(alimentadorUsuarios)
            .exec(ConfiguracaoPerformance.autenticar())
            .exec(
                    http("Listar Eventos Ativos")
                            .get("/api/eventos")
                            .headers(ConfiguracaoPerformance.cabecalhoAutenticado())
                            .check(status().is(200))
                            .check(jsonPath("$[0].id").optional().saveAs("eventoId"))
            )
            .exec(
                    doIf(session -> session.contains("eventoId")).then(
                            http("Detalhar Evento por ID")
                                    .get("/api/eventos/#{eventoId}")
                                    .headers(ConfiguracaoPerformance.cabecalhoAutenticado())
                                    .check(status().is(200))
                    )
            );

    public ConsultaEventosSimulation() {
        setUp(
                cenarioConsultaEventos.injectOpen(ConfiguracaoPerformance.obterInjecaoPadrao())
        )
        .protocols(ConfiguracaoPerformance.obterHttpProtocol())
        .assertions(
                global().successfulRequests().percent().gte(99.0),
                global().responseTime().percentile3().lt(200), // p95 < 200ms (Cache Redis)
                global().responseTime().percentile4().lt(500)  // p99 < 500ms
        );
    }
}
