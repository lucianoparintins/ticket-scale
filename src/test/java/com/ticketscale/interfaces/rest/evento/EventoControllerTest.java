package com.ticketscale.interfaces.rest.evento;

import com.ticketscale.application.evento.EventoService;
import com.ticketscale.domain.evento.Evento;
import com.ticketscale.domain.evento.PeriodoEvento;
import com.ticketscale.infrastructure.security.SecurityFilter;
import com.ticketscale.infrastructure.security.TokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventoController.class)
@AutoConfigureMockMvc(addFilters = false) // Desabilita segurança para teste unitário do controller
class EventoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventoService service;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private SecurityFilter securityFilter;

    @Test
    @DisplayName("Deve retornar 201 ao cadastrar um evento válido")
    void deveRetornar201AoCadastrarEventoValido() throws Exception {
        var id = UUID.randomUUID();
        var dataInicio = LocalDateTime.now().plusDays(1);
        var dataFim = LocalDateTime.now().plusDays(2);
        var evento = Evento.builder()
                .id(id)
                .nome("Evento Teste")
                .descricao("Descricao Teste")
                .periodo(new PeriodoEvento(dataInicio, dataFim))
                .build();

        when(service.criar(any(), any(), any(), any())).thenReturn(evento);

        var json = """
                {
                    "nome": "Evento Teste",
                    "descricao": "Descricao Teste",
                    "dataInicio": "%s",
                    "dataFim": "%s"
                }
                """.formatted(dataInicio, dataFim);

        mockMvc.perform(post("/eventos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.nome").value("Evento Teste"));
    }

    @Test
    @DisplayName("Deve retornar 400 ao cadastrar um evento com dados inválidos")
    void deveRetornar400AoCadastrarEventoInvalido() throws Exception {
        var json = "{}";

        mockMvc.perform(post("/eventos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }
}
