package com.ticketscale.domain.reserva;

import com.ticketscale.domain.evento.Evento;
import com.ticketscale.domain.evento.PeriodoEvento;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class IngressoTest {

    @Test
    void reservar_deveAlterarStatusParaReservado_quandoIngressoLivre() {
        Evento evento = Evento.builder()
                .nome("Evento")
                .periodo(new PeriodoEvento(LocalDateTime.now(), LocalDateTime.now().plusDays(1)))
                .build();
        Lote lote = Lote.builder()
                .evento(evento)
                .nome("Lote 1")
                .preco(BigDecimal.TEN)
                .capacidade(100)
                .build();
        Ingresso ingresso = Ingresso.builder()
                .lote(lote)
                .status(StatusIngresso.LIVRE)
                .build();

        ingresso.reservar();

        assertEquals(StatusIngresso.RESERVADO, ingresso.getStatus());
    }

    @Test
    void reservar_deveLancarExcecao_quandoIngressoNaoLivre() {
        Evento evento = Evento.builder()
                .nome("Evento")
                .periodo(new PeriodoEvento(LocalDateTime.now(), LocalDateTime.now().plusDays(1)))
                .build();
        Lote lote = Lote.builder()
                .evento(evento)
                .nome("Lote 1")
                .preco(BigDecimal.TEN)
                .capacidade(100)
                .build();
        Ingresso ingresso = Ingresso.builder()
                .lote(lote)
                .status(StatusIngresso.RESERVADO)
                .build();

        IllegalStateException exception = assertThrows(IllegalStateException.class, ingresso::reservar);
        assertEquals("Ingresso não está livre para reserva.", exception.getMessage());
    }

    @Test
    void vender_deveAlterarStatusParaVendido_quandoIngressoReservado() {
        Evento evento = Evento.builder()
                .nome("Evento")
                .periodo(new PeriodoEvento(LocalDateTime.now(), LocalDateTime.now().plusDays(1)))
                .build();
        Lote lote = Lote.builder()
                .evento(evento)
                .nome("Lote 1")
                .preco(BigDecimal.TEN)
                .capacidade(100)
                .build();
        Ingresso ingresso = Ingresso.builder()
                .lote(lote)
                .status(StatusIngresso.RESERVADO)
                .build();

        ingresso.vender();

        assertEquals(StatusIngresso.VENDIDO, ingresso.getStatus());
    }

    @Test
    void liberar_deveRestaurarStatusParaLivre() {
        Evento evento = Evento.builder()
                .nome("Evento")
                .periodo(new PeriodoEvento(LocalDateTime.now(), LocalDateTime.now().plusDays(1)))
                .build();
        Lote lote = Lote.builder()
                .evento(evento)
                .nome("Lote 1")
                .preco(BigDecimal.TEN)
                .capacidade(100)
                .build();
        Ingresso ingresso = Ingresso.builder()
                .lote(lote)
                .status(StatusIngresso.RESERVADO)
                .build();

        ingresso.liberar();

        assertEquals(StatusIngresso.LIVRE, ingresso.getStatus());
    }
}
