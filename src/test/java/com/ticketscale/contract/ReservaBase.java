package com.ticketscale.contract;

import com.ticketscale.application.usecase.ReservarIngressoUseCase;
import com.ticketscale.domain.evento.Evento;
import com.ticketscale.domain.evento.PeriodoEvento;
import com.ticketscale.domain.reserva.Ingresso;
import com.ticketscale.domain.reserva.Lote;
import com.ticketscale.domain.reserva.Reserva;
import com.ticketscale.domain.reserva.StatusIngresso;
import com.ticketscale.domain.reserva.StatusReserva;
import com.ticketscale.domain.usuario.Papel;
import com.ticketscale.domain.usuario.Usuario;
import com.ticketscale.interfaces.rest.ReservaController;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Classe base para os testes de contrato gerados para o endpoint de reserva de ingressos.
 */
public abstract class ReservaBase {

    static {
        SpringCompatibilityPatcher.apply();
    }

    @BeforeEach
    void setup() {
        ReservarIngressoUseCase useCase = Mockito.mock(ReservarIngressoUseCase.class);

        UUID loteId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID usuarioId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UUID reservaId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        UUID ingressoId = UUID.fromString("44444444-4444-4444-4444-444444444444");

        Evento evento = Evento.builder()
                .id(UUID.randomUUID())
                .nome("Evento Contrato")
                .periodo(new PeriodoEvento(LocalDateTime.now(), LocalDateTime.now().plusDays(1)))
                .build();

        Lote lote = Lote.builder()
                .id(loteId)
                .evento(evento)
                .nome("Lote Contrato")
                .preco(BigDecimal.TEN)
                .capacidade(100)
                .build();

        Usuario usuario = new Usuario(usuarioId, "usuario.teste", "senha123", Papel.USUARIO);
        Ingresso ingresso = Ingresso.builder()
                .id(ingressoId)
                .lote(lote)
                .status(StatusIngresso.RESERVADO)
                .build();

        Reserva reserva = Reserva.builder()
                .id(reservaId)
                .usuario(usuario)
                .ingresso(ingresso)
                .status(StatusReserva.PENDENTE)
                .dataCriacao(LocalDateTime.parse("2026-09-12T07:45:00"))
                .dataExpiracao(LocalDateTime.parse("2026-09-12T07:55:00"))
                .build();

        when(useCase.executar(eq(loteId), eq(usuarioId))).thenReturn(reserva);

        ReservaController controller = new ReservaController(useCase);
        RestAssuredMockMvc.standaloneSetup(controller);
    }
}
