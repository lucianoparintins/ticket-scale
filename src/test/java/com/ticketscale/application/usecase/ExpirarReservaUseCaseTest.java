package com.ticketscale.application.usecase;

import com.ticketscale.application.port.out.EventPublisher;
import com.ticketscale.application.port.out.LockManager;
import com.ticketscale.domain.event.CacheInvalidadoEvent;
import com.ticketscale.domain.event.ReservaExpiradaEvent;
import com.ticketscale.domain.evento.Evento;
import com.ticketscale.domain.evento.PeriodoEvento;
import com.ticketscale.domain.reserva.*;
import com.ticketscale.domain.usuario.Papel;
import com.ticketscale.domain.usuario.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpirarReservaUseCaseTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private LockManager lockManager;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private ExpirarReservaUseCase useCase;

    private UUID reservaId;
    private Reserva reserva;
    private Ingresso ingresso;
    private Lote lote;
    private Evento evento;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        reservaId = UUID.randomUUID();

        evento = Evento.builder()
                .id(UUID.randomUUID())
                .nome("Show de Rock")
                .periodo(new PeriodoEvento(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2)))
                .build();

        lote = Lote.builder()
                .id(UUID.randomUUID())
                .evento(evento)
                .nome("Lote Promocional")
                .preco(BigDecimal.valueOf(150))
                .capacidade(50)
                .build();

        ingresso = Ingresso.builder()
                .id(UUID.randomUUID())
                .lote(lote)
                .status(StatusIngresso.RESERVADO)
                .build();

        usuario = new Usuario(UUID.randomUUID(), "maria", "senha123", Papel.USUARIO);

        reserva = Reserva.builder()
                .id(reservaId)
                .usuario(usuario)
                .ingresso(ingresso)
                .status(StatusReserva.PENDENTE)
                .build();
    }

    @Test
    void executar_deveExpirarReservaPendenteELiberarIngresso_comSucesso() {
        when(lockManager.acquireLock("lock:pagamento:reserva:" + reservaId, 10)).thenReturn(true);
        when(reservaRepository.buscarComIngressoELotePorId(reservaId)).thenReturn(Optional.of(reserva));

        useCase.executar(reservaId);

        assertEquals(StatusReserva.CANCELADA, reserva.getStatus());
        assertEquals(StatusIngresso.LIVRE, ingresso.getStatus());

        verify(reservaRepository).save(reserva);
        verify(eventPublisher).publicarReservaExpirada(any(ReservaExpiradaEvent.class));
        verify(eventPublisher).publicarInvalidacaoCache(any(CacheInvalidadoEvent.class));
        verify(lockManager).releaseLock("lock:pagamento:reserva:" + reservaId);
    }

    @Test
    void executar_deveIgnorarExpiraCao_quandoReservaJaEstiverConfirmada() {
        reserva = Reserva.builder()
                .id(reservaId)
                .usuario(usuario)
                .ingresso(ingresso)
                .status(StatusReserva.CONFIRMADA)
                .build();

        when(lockManager.acquireLock("lock:pagamento:reserva:" + reservaId, 10)).thenReturn(true);
        when(reservaRepository.buscarComIngressoELotePorId(reservaId)).thenReturn(Optional.of(reserva));

        useCase.executar(reservaId);

        assertEquals(StatusReserva.CONFIRMADA, reserva.getStatus());
        verify(reservaRepository, never()).save(any());
        verify(eventPublisher, never()).publicarReservaExpirada(any());
        verify(eventPublisher, never()).publicarInvalidacaoCache(any());
        verify(lockManager).releaseLock("lock:pagamento:reserva:" + reservaId);
    }

    @Test
    void executar_deveIgnorarExpiraCao_quandoReservaJaEstiverCancelada() {
        reserva = Reserva.builder()
                .id(reservaId)
                .usuario(usuario)
                .ingresso(ingresso)
                .status(StatusReserva.CANCELADA)
                .build();

        when(lockManager.acquireLock("lock:pagamento:reserva:" + reservaId, 10)).thenReturn(true);
        when(reservaRepository.buscarComIngressoELotePorId(reservaId)).thenReturn(Optional.of(reserva));

        useCase.executar(reservaId);

        assertEquals(StatusReserva.CANCELADA, reserva.getStatus());
        verify(reservaRepository, never()).save(any());
        verify(eventPublisher, never()).publicarReservaExpirada(any());
        verify(eventPublisher, never()).publicarInvalidacaoCache(any());
        verify(lockManager).releaseLock("lock:pagamento:reserva:" + reservaId);
    }

    @Test
    void executar_deveIgnorarExpiraCao_quandoReservaNaoForEncontrada() {
        when(lockManager.acquireLock("lock:pagamento:reserva:" + reservaId, 10)).thenReturn(true);
        when(reservaRepository.buscarComIngressoELotePorId(reservaId)).thenReturn(Optional.empty());

        useCase.executar(reservaId);

        verify(reservaRepository, never()).save(any());
        verify(eventPublisher, never()).publicarReservaExpirada(any());
        verify(lockManager).releaseLock("lock:pagamento:reserva:" + reservaId);
    }

    @Test
    void executar_deveLancarExcecao_quandoNaoConseguirAdquirirLock() {
        when(lockManager.acquireLock("lock:pagamento:reserva:" + reservaId, 10)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> useCase.executar(reservaId));
        assertTrue(ex.getMessage().contains("Não foi possível adquirir o lock"));

        verify(reservaRepository, never()).buscarComIngressoELotePorId(any());
        verify(lockManager, never()).releaseLock(any());
    }
}
