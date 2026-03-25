package com.ticketscale.domain.reserva;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class IngressoTest {

    @Test
    void reservar_deveAlterarStatusParaReservado_quandoIngressoLivre() {
        Lote lote = new Lote(UUID.randomUUID(), null, "Lote 1", BigDecimal.TEN, 100);
        Ingresso ingresso = new Ingresso(UUID.randomUUID(), lote, StatusIngresso.LIVRE);

        ingresso.reservar();

        assertEquals(StatusIngresso.RESERVADO, ingresso.getStatus());
    }

    @Test
    void reservar_deveLancarExcecao_quandoIngressoNaoLivre() {
        Lote lote = new Lote(UUID.randomUUID(), null, "Lote 1", BigDecimal.TEN, 100);
        Ingresso ingresso = new Ingresso(UUID.randomUUID(), lote, StatusIngresso.RESERVADO);

        IllegalStateException exception = assertThrows(IllegalStateException.class, ingresso::reservar);
        assertEquals("Ingresso não está livre para reserva.", exception.getMessage());
    }

    @Test
    void vender_deveAlterarStatusParaVendido_quandoIngressoReservado() {
        Lote lote = new Lote(UUID.randomUUID(), null, "Lote 1", BigDecimal.TEN, 100);
        Ingresso ingresso = new Ingresso(UUID.randomUUID(), lote, StatusIngresso.RESERVADO);

        ingresso.vender();

        assertEquals(StatusIngresso.VENDIDO, ingresso.getStatus());
    }

    @Test
    void liberar_deveRestaurarStatusParaLivre() {
        Lote lote = new Lote(UUID.randomUUID(), null, "Lote 1", BigDecimal.TEN, 100);
        Ingresso ingresso = new Ingresso(UUID.randomUUID(), lote, StatusIngresso.RESERVADO);

        ingresso.liberar();

        assertEquals(StatusIngresso.LIVRE, ingresso.getStatus());
    }
}
