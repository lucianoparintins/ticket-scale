package com.ticketscale.performance;

import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.csv;
import static io.gatling.javaapi.core.CoreDsl.global;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

/**
 * Simulação de alta concorrência na reserva de ingressos (disputa de lote).
 * Valida a eficácia do Lock Distribuído Redis e a prevenção de overselling.
 */
public class ReservaConcorrenteSimulation extends Simulation {

    private final FeederBuilder<String> alimentadorUsuarios = csv("data/usuarios.csv").circular();
    private final FeederBuilder<String> alimentadorEventos = csv("data/eventos.csv").circular();

    private final ScenarioBuilder cenarioReservaConcorrente = scenario("Cenário de Reserva Concorrente com Lock")
            .feed(alimentadorUsuarios)
            .feed(alimentadorEventos)
            .exec(ConfiguracaoPerformance.autenticar())
            .exec(
                    http("Reservar Ingresso no Lote")
                            .post("/api/v1/reservas")
                            .headers(ConfiguracaoPerformance.cabecalhoAutenticado())
                            .body(StringBody("{\"loteId\":\"#{loteId}\",\"usuarioId\":\"#{usuarioId}\"}"))
                            // 201 = Reserva criada, 400/409/422 = Lote esgotado ou regra de negócio (válidos no esgotamento)
                            .check(status().in(201, 400, 409, 422))
                            .check(jsonPath("$.id").optional().saveAs("reservaId"))
            );

    public ReservaConcorrenteSimulation() {
        setUp(
                cenarioReservaConcorrente.injectOpen(ConfiguracaoPerformance.obterInjecaoPadrao())
        )
        .protocols(ConfiguracaoPerformance.obterHttpProtocol())
        .assertions(
                global().failedRequests().percent().lte(1.0),
                global().responseTime().percentile3().lt(1000), // p95 < 1s
                global().responseTime().percentile4().lt(2000)  // p99 < 2s
        );
    }
}
