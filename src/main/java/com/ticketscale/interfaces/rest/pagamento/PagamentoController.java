package com.ticketscale.interfaces.rest.pagamento;

import com.ticketscale.application.port.out.SolicitacaoPagamento;
import com.ticketscale.application.usecase.ProcessarPagamentoUseCase;
import com.ticketscale.domain.pagamento.Pagamento;
import com.ticketscale.domain.pagamento.PagamentoRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/pagamentos")
public class PagamentoController {

    private final ProcessarPagamentoUseCase processarPagamentoUseCase;
    private final PagamentoRepository pagamentoRepository;

    public PagamentoController(ProcessarPagamentoUseCase processarPagamentoUseCase, PagamentoRepository pagamentoRepository) {
        this.processarPagamentoUseCase = processarPagamentoUseCase;
        this.pagamentoRepository = pagamentoRepository;
    }

    @PostMapping
    public ResponseEntity<PagamentoResponseDTO> pagar(
            @RequestBody @Valid PagamentoRequestDTO request,
            UriComponentsBuilder uriBuilder) {
        
        SolicitacaoPagamento solicitacao = new SolicitacaoPagamento(
                request.reservaId(),
                null, // Valor será buscado no UseCase via Lote
                request.metodoPagamento(),
                request.toDadosMetodo()
        );

        processarPagamentoUseCase.executar(solicitacao);

        // Busca o pagamento criado (se chegou aqui, foi aprovado ou lançou exceção)
        // Nota: O UseCase garante que se não lançar exceção, o pagamento foi salvo e aprovado.
        Pagamento pagamento = pagamentoRepository.buscarPorReservaId(request.reservaId())
                .orElseThrow(() -> new RuntimeException("Erro ao recuperar pagamento processado."));

        PagamentoResponseDTO response = new PagamentoResponseDTO(
                pagamento.getId(),
                pagamento.getReservaId(),
                pagamento.getValor(),
                pagamento.getStatus(),
                pagamento.getMetodoPagamento(),
                pagamento.getTransacaoExternaId(),
                pagamento.getDataCriacao()
        );

        URI uri = uriBuilder.path("/api/v1/pagamentos/{id}").buildAndExpand(pagamento.getId()).toUri();
        return ResponseEntity.created(uri).body(response);
    }
}
