package com.ticketscale.application.reserva;

import com.ticketscale.application.port.out.EventPublisher;
import com.ticketscale.domain.evento.Evento;
import com.ticketscale.domain.evento.PeriodoEvento;
import com.ticketscale.domain.reserva.Lote;
import com.ticketscale.domain.reserva.LoteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoteServiceTest {

    @Mock
    private LoteRepository repository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private LoteService service;

    @Test
    @DisplayName("Deve buscar um lote por ID")
    void deveBuscarPorId() {
        UUID id = UUID.randomUUID();
        Lote lote = Lote.builder()
                .id(id)
                .evento(mock(Evento.class))
                .nome("Lote 1")
                .preco(BigDecimal.TEN)
                .capacidade(100)
                .build();

        when(repository.findById(id)).thenReturn(Optional.of(lote));

        Lote resultado = service.buscarPorId(id);

        assertNotNull(resultado);
        assertEquals(id, resultado.getId());
    }

    @Test
    @DisplayName("Deve atualizar um lote e publicar evento de invalidação")
    void deveAtualizarLoteEPublicarEvento() {
        UUID id = UUID.randomUUID();
        Evento evento = Evento.builder()
                .nome("Evento")
                .periodo(new PeriodoEvento(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2)))
                .build();
        
        Lote lote = Lote.builder()
                .id(id)
                .evento(evento)
                .nome("Lote Antigo")
                .preco(BigDecimal.TEN)
                .capacidade(100)
                .build();

        when(repository.findById(id)).thenReturn(Optional.of(lote));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Lote resultado = service.atualizar(id, "Lote Novo", BigDecimal.valueOf(20), 200);

        assertNotNull(resultado);
        assertEquals("Lote Novo", resultado.getNome());
        assertEquals(BigDecimal.valueOf(20), resultado.getPreco());
        
        verify(repository).save(any());
        verify(eventPublisher).publicarInvalidacaoCache(any());
    }
}
