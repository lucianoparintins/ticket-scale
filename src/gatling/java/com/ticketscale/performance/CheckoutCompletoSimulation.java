package com.ticketscale.performance;

import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.csv;
import static io.gatling.javaapi.core.CoreDsl.doIf;
import static io.gatling.javaapi.core.CoreDsl.global;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

/**
 * Simulação de ponta a ponta (E2E) do fluxo completo de compra:
 * Login -> Consulta de Eventos -> Reserva de Ingresso -> Processamento de Pagamento.
 */
public class CheckoutCompletoSimulation extends Simulation {

    private final FeederBuilder<String> alimentadorUsuarios = csv("data/usuarios.csv").circular();
    private final FeederBuilder<String> alimentadorEventos = csv("data/eventos.csv").circular();

    private final ScenarioBuilder cenarioCheckoutCompleto = scenario("Cenário de Checkout Completo E2E")
            .feed(alimentadorUsuarios)
            .feed(alimentadorEventos)
            // 1. Autenticação
            .exec(ConfiguracaoPerformance.autenticar())
            // 2. Consulta de Eventos no Catálogo
            .exec(
                    http("Consulta - Listar Eventos")
                            .get("/api/eventos")
                            .headers(ConfiguracaoPerformance.cabecalhoAutenticado())
                            .check(status().is(200))
            )
            // 3. Reserva do Ingresso
            .exec(
                    http("Reserva - Solicitar Reserva")
                            .post("/api/v1/reservas")
                            .headers(ConfiguracaoPerformance.cabecalhoAutenticado())
                            .body(StringBody("{\"loteId\":\"#{loteId}\",\"usuarioId\":\"#{usuarioId}\"}"))
                            .check(status().in(201, 400, 409, 422))
                            .check(jsonPath("$.id").optional().saveAs("reservaId"))
            )
            // 4. Pagamento da Reserva (apenas se a reserva foi criada com sucesso)
            .exec(
                    doIf(session -> session.contains("reservaId")).then(
                            http("Pagamento - Processar PIX")
                                    .post("/api/v1/pagamentos")
                                    .headers(ConfiguracaoPerformance.cabecalhoAutenticado())
                                    .body(StringBody("{\"reservaId\":\"#{reservaId}\","
                                            + "\"metodoPagamento\":\"PIX\","
                                            + "\"dadosPix\":{\"chavePix\":\"pagamento-gatling@ticketscale.com\"}}"))
                                    .check(status().in(201, 400, 409, 422))
                    )
            );

    public CheckoutCompletoSimulation() {
        setUp(
                cenarioCheckoutCompleto.injectOpen(ConfiguracaoPerformance.obterInjecaoPadrao())
        )
        .protocols(ConfiguracaoPerformance.obterHttpProtocol())
        .assertions(
                global().failedRequests().percent().lte(2.0),
                global().responseTime().percentile3().lt(1500), // p95 < 1.5s
                global().responseTime().percentile4().lt(3000)  // p99 < 3s
        );
    }
}
