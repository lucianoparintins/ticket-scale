package com.ticketscale.application.usecase;

import com.ticketscale.application.ports.LockManager;
import com.ticketscale.domain.evento.Evento;
import com.ticketscale.domain.evento.PeriodoEvento;
import com.ticketscale.domain.reserva.*;
import com.ticketscale.domain.usuario.Usuario;
import com.ticketscale.domain.usuario.UsuarioRepository;
import com.ticketscale.domain.usuario.Papel;
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
class ReservarIngressoUseCaseTest {

    @Mock
    private IngressoRepository ingressoRepository;

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private LoteRepository loteRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private LockManager lockManager;

    @InjectMocks
    private ReservarIngressoUseCase useCase;

    private UUID loteId;
    private UUID usuarioId;
    private Lote lote;
    private Usuario usuario;
    private Ingresso ingressoLivre;

    @BeforeEach
    void setUp() {
        loteId = UUID.randomUUID();
        usuarioId = UUID.randomUUID();
        
        Evento evento = Evento.builder()
                .id(UUID.randomUUID())
                .nome("Evento Teste")
                .periodo(new PeriodoEvento(LocalDateTime.now(), LocalDateTime.now().plusDays(1)))
                .build();
        
        lote = Lote.builder()
                .id(loteId)
                .evento(evento)
                .nome("Lote 1")
                .preco(BigDecimal.TEN)
                .capacidade(100)
                .build();
        
        usuario = new Usuario(usuarioId, "john", "pass", Papel.USUARIO);
        
        ingressoLivre = Ingresso.builder()
                .lote(lote)
                .status(StatusIngresso.LIVRE)
                .build();
    }

    @Test
    void executar_deveReservarComSucesso_quandoLockAdquiridoEIngressoDisponivel() {
        when(lockManager.acquireLock("lock:reserva:lote:" + loteId, 10)).thenReturn(true);
        when(loteRepository.findById(loteId)).thenReturn(Optional.of(lote));
        when(ingressoRepository.findFirstByLoteIdAndStatus(loteId, StatusIngresso.LIVRE)).thenReturn(Optional.of(ingressoLivre));
        when(usuarioRepository.buscarPorId(usuarioId)).thenReturn(Optional.of(usuario));
        
        Reserva reservaMock = new Reserva(usuario, ingressoLivre);
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reservaMock);

        Reserva reservaSalva = useCase.executar(loteId, usuarioId);

        assertNotNull(reservaSalva);
        assertEquals(StatusIngresso.RESERVADO, ingressoLivre.getStatus());
        verify(ingressoRepository).save(ingressoLivre);
        verify(reservaRepository).save(any(Reserva.class));
        verify(lockManager).releaseLock("lock:reserva:lote:" + loteId);
    }

    @Test
    void executar_deveLancarExcecao_quandoLockFalhar() {
        when(lockManager.acquireLock("lock:reserva:lote:" + loteId, 10)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> useCase.executar(loteId, usuarioId));
        assertTrue(exception.getMessage().contains("Não foi possível adquirir o lock"));

        verify(loteRepository, never()).findById(any());
        verify(lockManager, never()).releaseLock(any());
    }

    @Test
    void executar_deveLancarExcecaoELiberarLock_quandoIngressoNaoDisponivel() {
        when(lockManager.acquireLock("lock:reserva:lote:" + loteId, 10)).thenReturn(true);
        when(loteRepository.findById(loteId)).thenReturn(Optional.of(lote));
        when(ingressoRepository.findFirstByLoteIdAndStatus(loteId, StatusIngresso.LIVRE)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> useCase.executar(loteId, usuarioId));
        assertTrue(exception.getMessage().contains("Ingressos esgotados"));

        verify(lockManager).releaseLock("lock:reserva:lote:" + loteId);
        verify(reservaRepository, never()).save(any());
    }
}
