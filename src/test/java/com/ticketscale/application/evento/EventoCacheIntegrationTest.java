package com.ticketscale.application.evento;

import com.ticketscale.application.port.out.EventPublisher;
import com.ticketscale.config.TestConfig;
import com.ticketscale.domain.evento.Evento;
import com.ticketscale.domain.evento.EventoRepository;
import com.ticketscale.domain.evento.PeriodoEvento;
import com.ticketscale.infrastructure.config.CacheConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
class EventoCacheIntegrationTest {

    @Autowired
    private EventoService eventoService;

    @MockitoBean
    private EventoRepository eventoRepository;

    @MockitoBean
    private EventPublisher eventPublisher;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void cleanCache() {
        var cache = cacheManager.getCache(CacheConfig.CACHE_EVENTOS);
        if (cache != null) {
            cache.clear();
        }
    }

    @Test
    @DisplayName("Deve buscar evento no banco apenas na primeira chamada e depois no cache")
    void deveUsarCacheAoBuscarPorId() {
        UUID id = UUID.randomUUID();
        Evento evento = Evento.builder()
                .id(id)
                .nome("Evento Cache")
                .periodo(new PeriodoEvento(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2)))
                .build();

        when(eventoRepository.buscarPorId(id)).thenReturn(Optional.of(evento));

        // Primeira chamada: deve ir ao repositório
        Evento e1 = eventoService.buscarPorId(id);
        
        // Segunda chamada: deve vir do cache
        Evento e2 = eventoService.buscarPorId(id);

        assertEquals(e1, e2);
        verify(eventoRepository, times(1)).buscarPorId(id);
    }

    @Test
    @DisplayName("Deve invalidar cache de lista após criar novo evento")
    void deveInvalidarCacheAosCriar() {
        when(eventoRepository.listarAtivos()).thenReturn(List.of());

        // Popula cache de lista
        eventoService.listarAtivos();
        eventoService.listarAtivos();
        
        verify(eventoRepository, times(1)).listarAtivos();

        // Cria evento (deve disparar @CacheEvict allEntries=true)
        eventoService.criar("Novo", "Desc", LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        // Chama lista novamente: deve ir ao repositório de novo
        eventoService.listarAtivos();
        
        verify(eventoRepository, times(2)).listarAtivos();
    }
}
