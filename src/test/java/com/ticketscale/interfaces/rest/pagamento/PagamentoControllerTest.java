package com.ticketscale.interfaces.rest.pagamento;

import com.ticketscale.application.usecase.ProcessarPagamentoUseCase;
import com.ticketscale.domain.pagamento.*;
import com.ticketscale.infrastructure.security.SecurityFilter;
import com.ticketscale.infrastructure.security.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PagamentoController.class)
@AutoConfigureMockMvc(addFilters = false)
class PagamentoControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private ProcessarPagamentoUseCase processarPagamentoUseCase;
    @MockitoBean private PagamentoRepository pagamentoRepository;
    @MockitoBean private TokenService tokenService; 
    @MockitoBean private SecurityFilter securityFilter;

    @Test
    void pagar_DeveRetornarStatus201_QuandoPayloadForValido() throws Exception {
        UUID reservaId = UUID.randomUUID();
        Pagamento pagamento = new Pagamento(reservaId, BigDecimal.TEN, MetodoPagamento.PIX);
        // Não preciso confirmar() se o UseCase processar com sucesso, 
        // mas aqui estamos mockando o repositório então precisamos de um objeto pronto
        pagamento.confirmar("PIX-123");

        when(pagamentoRepository.buscarPorReservaId(reservaId)).thenReturn(Optional.of(pagamento));

        mockMvc.perform(post("/api/v1/pagamentos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "reservaId": "%s",
                            "metodoPagamento": "PIX",
                            "dadosPix": {
                                "chavePix": "minha-chave"
                            }
                        }
                        """.formatted(reservaId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("APROVADO"))
                .andExpect(jsonPath("$.metodoPagamento").value("PIX"))
                .andExpect(header().exists("Location"));
    }

    @Test
    void pagar_DeveRetornar404_QuandoReservaNaoEncontrada() throws Exception {
        UUID reservaId = UUID.randomUUID();
        doThrow(new ReservaNaoEncontradaException("Reserva não encontrada")).when(processarPagamentoUseCase).executar(any());

        mockMvc.perform(post("/api/v1/pagamentos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "reservaId": "%s",
                            "metodoPagamento": "PIX",
                            "dadosPix": {
                                "chavePix": "minha-chave"
                            }
                        }
                        """.formatted(reservaId)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("RESERVA_NAO_ENCONTRADA"));
    }
}
