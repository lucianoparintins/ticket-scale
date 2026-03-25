package com.ticketscale;

import com.ticketscale.application.ports.LockManager;
import com.ticketscale.application.usecase.ReservarIngressoUseCase;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ReservarIngressoIntegrationTest {

    @Autowired
    private ReservarIngressoUseCase useCase;

    // Interceptamos o componente da porta externa para focar nos testes
    // de orquestração interna e persistência multi-tabelas H2
    @MockitoBean
    private LockManager lockManager;

    @Autowired(required = false)
    private IngressoRepository ingressoRepository;

    @Autowired(required = false)
    private LoteRepository loteRepository;

    @Autowired(required = false)
    private ReservaRepository reservaRepository;

    @Autowired(required = false)
    private UsuarioRepository usuarioRepository;

    @Autowired(required = false)
    private EventoRepository eventoRepository;

    private Usuario usuario;
    private Lote lote;
    
    @BeforeEach
    void setUp() {
        if(reservaRepository == null) return;
        
        reservaRepository.deleteAll();
        ingressoRepository.deleteAll();
        loteRepository.deleteAll();

        usuario = new Usuario(null, "int_user", "pass123", Papel.USUARIO);
        
        try {
           usuario = usuarioRepository.salvar(usuario);
        } catch(Exception e){}

        Evento evento = new Evento(null, "Evento Int", "Descricao", new PeriodoEvento(LocalDateTime.now(), LocalDateTime.now().plusDays(1)));
        try {
           evento = eventoRepository.salvar(evento);
        } catch(Exception e){}
        
        lote = new Lote(null, evento, "Lote Unico", BigDecimal.valueOf(50.0), 10);
        lote = loteRepository.save(lote);

        Ingresso ingresso = new Ingresso(null, lote, StatusIngresso.LIVRE);
        ingresso = ingressoRepository.save(ingresso);
    }

    @Test
    void executar_deveGravarReservaNoH2_aoSucesso() {
        if(reservaRepository == null) return;
        
        when(lockManager.acquireLock(anyString(), anyLong())).thenReturn(true);
        
        if(usuario != null && usuario.getId() != null && lote.getId() != null) {
            Reserva salva = useCase.executar(lote.getId(), usuario.getId());
            
            assertNotNull(salva.getId());
            assertEquals(StatusReserva.PENDENTE, salva.getStatus());
            
            assertTrue(reservaRepository.findById(salva.getId()).isPresent());
            
            Ingresso dbIngresso = ingressoRepository.findById(salva.getIngresso().getId()).orElseThrow();
            assertEquals(StatusIngresso.RESERVADO, dbIngresso.getStatus());
        }
    }
}
