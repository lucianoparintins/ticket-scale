package com.ticketscale.domain.reserva;

import com.ticketscale.domain.evento.Evento;
import com.ticketscale.domain.evento.PeriodoEvento;
import com.ticketscale.domain.usuario.Usuario;
import com.ticketscale.domain.usuario.Papel;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class ReservaTest {

    @Test
    void isExpirada_deveRetornarTrue_quandoExpirouEStatusPendente() throws Exception {
        Usuario usuario = new Usuario(UUID.randomUUID(), "john", "pass", Papel.USUARIO);
        Evento evento = Evento.builder()
                .nome("Evento")
                .periodo(new PeriodoEvento(LocalDateTime.now(), LocalDateTime.now().plusDays(1)))
                .build();
        Lote lote = Lote.builder()
                .evento(evento)
                .nome("Lote")
                .preco(BigDecimal.TEN)
                .capacidade(10)
                .build();
        Ingresso ingresso = Ingresso.builder()
                .lote(lote)
                .status(StatusIngresso.LIVRE)
                .build();

        Reserva reserva = new Reserva(usuario, ingresso);

        // Simular a data de expiração no passado via reflection (já que não usamos Clock injetável)
        var campo = Reserva.class.getDeclaredField("dataExpiracao");
        campo.setAccessible(true);
        campo.set(reserva, LocalDateTime.now().minusSeconds(1));

        assertTrue(reserva.isExpirada());
    }

    @Test
    void confirmarPagamento_deveAlterarStatusETemQueVenderIngresso() {
        Usuario usuario = new Usuario(UUID.randomUUID(), "john", "pass", Papel.USUARIO);
        Evento evento = Evento.builder()
                .nome("Evento")
                .periodo(new PeriodoEvento(LocalDateTime.now(), LocalDateTime.now().plusDays(1)))
                .build();
        Lote lote = Lote.builder()
                .evento(evento)
                .nome("Lote")
                .preco(BigDecimal.TEN)
                .capacidade(10)
                .build();

        // Simulando fluxo real
        Ingresso ingresso = Ingresso.builder()
                .lote(lote)
                .status(StatusIngresso.LIVRE)
                .build();
        ingresso.reservar();

        Reserva reserva = new Reserva(usuario, ingresso);
        reserva.confirmarPagamento();

        assertEquals(StatusReserva.CONFIRMADA, reserva.getStatus());
        assertEquals(StatusIngresso.VENDIDO, ingresso.getStatus());
    }

    @Test
    void cancelar_deveRestaurarIngressoLivreEAlterarReservaCancelada() {
        Usuario usuario = new Usuario(UUID.randomUUID(), "john", "pass", Papel.USUARIO);
        Evento evento = Evento.builder()
                .nome("Evento")
                .periodo(new PeriodoEvento(LocalDateTime.now(), LocalDateTime.now().plusDays(1)))
                .build();
        Lote lote = Lote.builder()
                .evento(evento)
                .nome("Lote")
                .preco(BigDecimal.TEN)
                .capacidade(10)
                .build();
        Ingresso ingresso = Ingresso.builder()
                .lote(lote)
                .status(StatusIngresso.LIVRE)
                .build();
        ingresso.reservar();

        Reserva reserva = new Reserva(usuario, ingresso);
        reserva.cancelar();

        assertEquals(StatusReserva.CANCELADA, reserva.getStatus());
        assertEquals(StatusIngresso.LIVRE, ingresso.getStatus());
    }
}
