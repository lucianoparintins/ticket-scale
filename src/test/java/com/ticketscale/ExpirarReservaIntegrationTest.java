package com.ticketscale;

import com.ticketscale.application.port.out.EventPublisher;
import com.ticketscale.application.port.out.LockManager;
import com.ticketscale.application.usecase.ExpirarReservaUseCase;
import com.ticketscale.application.usecase.ReservarIngressoUseCase;
import com.ticketscale.domain.event.ReservaExpiradaEvent;
import com.ticketscale.domain.evento.Evento;
import com.ticketscale.domain.evento.EventoRepository;
import com.ticketscale.domain.evento.PeriodoEvento;
import com.ticketscale.domain.reserva.*;
import com.ticketscale.domain.usuario.Papel;
import com.ticketscale.domain.usuario.Usuario;
import com.ticketscale.domain.usuario.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ExpirarReservaIntegrationTest {

    @Autowired
    private ReservarIngressoUseCase reservarIngressoUseCase;

    @Autowired
    private ExpirarReservaUseCase expirarReservaUseCase;

    @MockitoBean
    private LockManager lockManager;

    @MockitoBean
    private EventPublisher eventPublisher;

    @Autowired
    private IngressoRepository ingressoRepository;

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EventoRepository eventoRepository;

    @BeforeEach
    void setUp() {
        reservaRepository.deleteAll();
        ingressoRepository.deleteAll();
        loteRepository.deleteAll();
    }

    @Test
    void fluxoCompleto_criarReserva_eExpirarComSucesso_liberandoIngresso() {
        Usuario usuario = new Usuario(null, "usuario_exp_1", "pass123", Papel.USUARIO);
        usuario = usuarioRepository.salvar(usuario);

        Evento evento = Evento.builder()
                .nome("Festival de Música 1")
                .descricao("Festival anual")
                .periodo(new PeriodoEvento(LocalDateTime.now().plusDays(10), LocalDateTime.now().plusDays(11)))
                .build();
        evento = eventoRepository.salvar(evento);

        Lote lote = Lote.builder()
                .evento(evento)
                .nome("Pista Premium 1")
                .preco(BigDecimal.valueOf(250.0))
                .capacidade(100)
                .build();
        lote = loteRepository.save(lote);

        Ingresso ingresso = Ingresso.builder()
                .lote(lote)
                .status(StatusIngresso.LIVRE)
                .build();
        ingresso = ingressoRepository.save(ingresso);

        when(lockManager.acquireLock(anyString(), anyLong())).thenReturn(true);

        Reserva reservaCriada = reservarIngressoUseCase.executar(lote.getId(), usuario.getId());
        assertNotNull(reservaCriada.getId());
        assertEquals(StatusReserva.PENDENTE, reservaCriada.getStatus());

        Ingresso ingressoReservado = ingressoRepository.findById(reservaCriada.getIngresso().getId()).orElseThrow();
        assertEquals(StatusIngresso.RESERVADO, ingressoReservado.getStatus());

        expirarReservaUseCase.executar(reservaCriada.getId());

        Reserva reservaExpirada = reservaRepository.findById(reservaCriada.getId()).orElseThrow();
        assertEquals(StatusReserva.CANCELADA, reservaExpirada.getStatus());

        Ingresso ingressoLiberado = ingressoRepository.findById(reservaCriada.getIngresso().getId()).orElseThrow();
        assertEquals(StatusIngresso.LIVRE, ingressoLiberado.getStatus());

        verify(eventPublisher).publicarReservaExpirada(any(ReservaExpiradaEvent.class));
    }

    @Test
    void expirar_naoDeveAlterarReservaConfirmada() {
        Usuario usuario = new Usuario(null, "usuario_exp_2", "pass123", Papel.USUARIO);
        usuario = usuarioRepository.salvar(usuario);

        Evento evento = Evento.builder()
                .nome("Festival de Música 2")
                .descricao("Festival anual")
                .periodo(new PeriodoEvento(LocalDateTime.now().plusDays(10), LocalDateTime.now().plusDays(11)))
                .build();
        evento = eventoRepository.salvar(evento);

        Lote lote = Lote.builder()
                .evento(evento)
                .nome("Pista Premium 2")
                .preco(BigDecimal.valueOf(250.0))
                .capacidade(100)
                .build();
        lote = loteRepository.save(lote);

        Ingresso ingresso = Ingresso.builder()
                .lote(lote)
                .status(StatusIngresso.LIVRE)
                .build();
        ingresso = ingressoRepository.save(ingresso);

        when(lockManager.acquireLock(anyString(), anyLong())).thenReturn(true);

        Reserva reservaCriada = reservarIngressoUseCase.executar(lote.getId(), usuario.getId());
        reservaCriada.confirmarPagamento();
        ingressoRepository.save(reservaCriada.getIngresso());
        reservaRepository.save(reservaCriada);

        expirarReservaUseCase.executar(reservaCriada.getId());

        Reserva reservaNoBanco = reservaRepository.findById(reservaCriada.getId()).orElseThrow();
        assertEquals(StatusReserva.CONFIRMADA, reservaNoBanco.getStatus());

        Ingresso ingressoVendido = ingressoRepository.findById(reservaCriada.getIngresso().getId()).orElseThrow();
        assertEquals(StatusIngresso.VENDIDO, ingressoVendido.getStatus());
    }
}
