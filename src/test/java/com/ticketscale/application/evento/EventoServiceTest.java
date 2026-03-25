package com.ticketscale.application.evento;

import com.ticketscale.domain.evento.Evento;
import com.ticketscale.domain.evento.EventoRepository;
import com.ticketscale.domain.evento.PeriodoEvento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventoServiceTest {

    @Mock
    private EventoRepository repository;

    @InjectMocks
    private EventoService service;

    @Test
    @DisplayName("Deve criar um evento com sucesso")
    void deveCriarEvento() {
        var dataInicio = LocalDateTime.now().plusDays(1);
        var dataFim = LocalDateTime.now().plusDays(2);
        
        when(repository.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var evento = service.criar("Show", "Descrição", dataInicio, dataFim);

        assertNotNull(evento);
        assertEquals("Show", evento.getNome());
        verify(repository).salvar(any());
    }

    @Test
    @DisplayName("Não deve criar evento com data de fim retroativa")
    void naoDeveCriarEventoComDataRetroativa() {
        var dataInicio = LocalDateTime.now().plusDays(2);
        var dataFim = LocalDateTime.now().plusDays(1);

        assertThrows(IllegalArgumentException.class, () -> 
                service.criar("Show", "Descrição", dataInicio, dataFim));
    }

    @Test
    @DisplayName("Deve desativar um evento")
    void deveDesativarEvento() {
        var id = UUID.randomUUID();
        var evento = new Evento(id, "Show", "Desc", new PeriodoEvento(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2)));
        
        when(repository.buscarPorId(id)).thenReturn(Optional.of(evento));

        service.desativar(id);

        assertFalse(evento.isAtivo());
        verify(repository).salvar(evento);
    }
}
