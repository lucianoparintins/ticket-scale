package com.ticketscale.interfaces.rest;

import com.ticketscale.application.usecase.ReservarIngressoUseCase;
import com.ticketscale.domain.evento.Evento;
import com.ticketscale.domain.evento.PeriodoEvento;
import com.ticketscale.domain.reserva.*;
import com.ticketscale.domain.usuario.Papel;
import com.ticketscale.domain.usuario.Usuario;
import com.ticketscale.infrastructure.security.SecurityFilter;
import com.ticketscale.infrastructure.security.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservaController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReservaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservarIngressoUseCase reservarIngressoUseCase;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private SecurityFilter securityFilter;

    private UUID loteId;
    private UUID usuarioId;
    private Reserva reservaMock;

    @BeforeEach
    void setUp() {
        loteId = UUID.randomUUID();
        usuarioId = UUID.randomUUID();

        Evento evento = Evento.builder()
                .id(UUID.randomUUID())
                .nome("Evento Teste")
                .periodo(new PeriodoEvento(LocalDateTime.now(), LocalDateTime.now().plusDays(1)))
                .build();

        var lote = Lote.builder()
                .id(loteId)
                .evento(evento)
                .nome("Lote Teste")
                .preco(BigDecimal.TEN)
                .capacidade(50)
                .build();
        var ingresso = Ingresso.builder()
                .lote(lote)
                .status(StatusIngresso.RESERVADO)
                .build();
        var usuario = new Usuario(usuarioId, "tester", "pass", Papel.USUARIO);

        reservaMock = Reserva.builder()
                .id(UUID.randomUUID())
                .usuario(usuario)
                .ingresso(ingresso)
                .build();
    }

    @Test
    @DisplayName("Deve retornar 201 ao realizar reserva com payload válido")
    void reservar_deveRetornarStatus201_quandoPayloadForValido() throws Exception {
        when(reservarIngressoUseCase.executar(loteId, usuarioId)).thenReturn(reservaMock);

        var json = """
                {
                    "loteId": "%s",
                    "usuarioId": "%s"
                }
                """.formatted(loteId, usuarioId);

        mockMvc.perform(post("/api/v1/reservas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(reservaMock.getId().toString()))
                .andExpect(jsonPath("$.status").value(StatusReserva.PENDENTE.name()));
    }

    @Test
    @DisplayName("Deve retornar 400 ao realizar reserva com payload inválido")
    void reservar_deveRetornarStatus400_quandoPayloadForInvalido() throws Exception {
        var json = """
                {
                    "loteId": null,
                    "usuarioId": "%s"
                }
                """.formatted(usuarioId);

        mockMvc.perform(post("/api/v1/reservas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }
}
