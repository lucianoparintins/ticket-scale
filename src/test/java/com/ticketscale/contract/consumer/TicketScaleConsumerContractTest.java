package com.ticketscale.contract.consumer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.StubFinder;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Teste de integração do lado consumidor demonstrando o consumo dos stubs
 * gerados pelo Spring Cloud Contract para Autenticação e Reservas.
 */
@SpringBootTest(
        classes = TicketScaleConsumerContractTest.ConsumerTestConfig.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "stubrunner.ids=com.ticketscale:ticketscale:+:stubs",
                "stubrunner.stubs-mode=classpath"
        }
)
@AutoConfigureStubRunner
class TicketScaleConsumerContractTest {

    @Configuration
    @EnableAutoConfiguration
    static class ConsumerTestConfig {
    }

    @Autowired
    private StubFinder stubFinder;

    @Test
    @DisplayName("Consumidor deve autenticar com sucesso consumindo o stub do contrato de login")
    void deveAutenticarComSucessoUsandoStub() {
        int port = stubFinder.findStubUrl("ticketscale").getPort();
        RestClient restClient = RestClient.create("http://localhost:" + port);

        Map<String, String> loginRequest = Map.of(
                "login", "usuario.teste",
                "senha", "senhaValida123"
        );

        @SuppressWarnings("rawtypes")
        ResponseEntity<Map> response = restClient.post()
                .uri("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(loginRequest)
                .retrieve()
                .toEntity(Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("token_jwt_valido", response.getBody().get("token"));
    }

    @Test
    @DisplayName("Consumidor deve criar reserva com sucesso consumindo o stub do contrato de reservas")
    void deveCriarReservaComSucessoUsandoStub() {
        int port = stubFinder.findStubUrl("ticketscale").getPort();
        RestClient restClient = RestClient.create("http://localhost:" + port);

        Map<String, String> reservaRequest = Map.of(
                "loteId", "11111111-1111-1111-1111-111111111111",
                "usuarioId", "22222222-2222-2222-2222-222222222222"
        );

        @SuppressWarnings("rawtypes")
        ResponseEntity<Map> response = restClient.post()
                .uri("/api/v1/reservas")
                .contentType(MediaType.APPLICATION_JSON)
                .body(reservaRequest)
                .retrieve()
                .toEntity(Map.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("33333333-3333-3333-3333-333333333333", response.getBody().get("id"));
        assertEquals("22222222-2222-2222-2222-222222222222", response.getBody().get("usuarioId"));
        assertEquals("44444444-4444-4444-4444-444444444444", response.getBody().get("ingressoId"));
        assertEquals("PENDENTE", response.getBody().get("status"));
    }
}
