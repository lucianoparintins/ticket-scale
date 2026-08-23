package com.ticketscale.infrastructure.scheduler;

import com.ticketscale.application.usecase.ExpirarReservaUseCase;
import com.ticketscale.domain.evento.Evento;
import com.ticketscale.domain.evento.PeriodoEvento;
import com.ticketscale.domain.reserva.Ingresso;
import com.ticketscale.domain.reserva.Lote;
import com.ticketscale.domain.reserva.Reserva;
import com.ticketscale.domain.reserva.ReservaRepository;
import com.ticketscale.domain.usuario.Papel;
import com.ticketscale.domain.usuario.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpiracaoReservaSchedulerTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private ExpirarReservaUseCase expirarReservaUseCase;

    @InjectMocks
    private ExpiracaoReservaScheduler scheduler;

    @Test
    void verificarReservasExpiradas_deveProcessarTodasAsReservasEncontradas() {
        Usuario usuario = new Usuario(UUID.randomUUID(), "carlos", "senha", Papel.USUARIO);
        Evento evento = Evento.builder().nome("Evento").periodo(new PeriodoEvento(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2))).build();
        Lote lote = Lote.builder().id(UUID.randomUUID()).evento(evento).nome("Lote").preco(BigDecimal.TEN).capacidade(10).build();

        Ingresso ingresso1 = Ingresso.builder().id(UUID.randomUUID()).lote(lote).build();
        Ingresso ingresso2 = Ingresso.builder().id(UUID.randomUUID()).lote(lote).build();

        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        Reserva reserva1 = Reserva.builder().id(id1).usuario(usuario).ingresso(ingresso1).build();
        Reserva reserva2 = Reserva.builder().id(id2).usuario(usuario).ingresso(ingresso2).build();

        when(reservaRepository.buscarReservasExpiradas(any(LocalDateTime.class)))
                .thenReturn(List.of(reserva1, reserva2));

        scheduler.verificarReservasExpiradas();

        verify(expirarReservaUseCase).executar(id1);
        verify(expirarReservaUseCase).executar(id2);
    }

    @Test
    void verificarReservasExpiradas_deveContinuarMesmoSeUmaReservaLancarExcecao() {
        Usuario usuario = new Usuario(UUID.randomUUID(), "carlos", "senha", Papel.USUARIO);
        Evento evento = Evento.builder().nome("Evento").periodo(new PeriodoEvento(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2))).build();
        Lote lote = Lote.builder().id(UUID.randomUUID()).evento(evento).nome("Lote").preco(BigDecimal.TEN).capacidade(10).build();

        Ingresso ingresso1 = Ingresso.builder().id(UUID.randomUUID()).lote(lote).build();
        Ingresso ingresso2 = Ingresso.builder().id(UUID.randomUUID()).lote(lote).build();

        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        Reserva reserva1 = Reserva.builder().id(id1).usuario(usuario).ingresso(ingresso1).build();
        Reserva reserva2 = Reserva.builder().id(id2).usuario(usuario).ingresso(ingresso2).build();

        when(reservaRepository.buscarReservasExpiradas(any(LocalDateTime.class)))
                .thenReturn(List.of(reserva1, reserva2));

        doThrow(new RuntimeException("Lock error")).when(expirarReservaUseCase).executar(id1);

        scheduler.verificarReservasExpiradas();

        verify(expirarReservaUseCase).executar(id1);
        verify(expirarReservaUseCase).executar(id2);
    }
}
