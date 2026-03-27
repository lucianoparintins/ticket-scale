package com.ticketscale.interfaces.rest.pagamento;

import com.ticketscale.domain.pagamento.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class PagamentoExceptionHandler {

    @ExceptionHandler(ReservaNaoEncontradaException.class)
    public ResponseEntity<ErroResponseDTO> handleReservaNaoEncontrada(ReservaNaoEncontradaException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErroResponseDTO(ex.getMessage(), "RESERVA_NAO_ENCONTRADA"));
    }

    @ExceptionHandler(PagamentoDuplicadoException.class)
    public ResponseEntity<ErroResponseDTO> handlePagamentoDuplicado(PagamentoDuplicadoException x) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new ErroResponseDTO(x.getMessage(), "PAGAMENTO_DUPLICADO"));
    }

    @ExceptionHandler(MetodoNaoSuportadoException.class)
    public ResponseEntity<ErroResponseDTO> handleMetodoNaoSuportado(MetodoNaoSuportadoException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErroResponseDTO(ex.getMessage(), "METODO_NAO_SUPORTADO"));
    }

    @ExceptionHandler(PagamentoRecusadoException.class)
    public ResponseEntity<ErroResponseDTO> handlePagamentoRecusado(PagamentoRecusadoException ex) {
        return ResponseEntity.status(HttpStatus.valueOf(422))
            .body(new ErroResponseDTO(ex.getMessage(), "PAGAMENTO_RECUSADO"));
    }

    @ExceptionHandler(PagamentoException.class)
    public ResponseEntity<ErroResponseDTO> handlePagamentoGenerico(PagamentoException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErroResponseDTO(ex.getMessage(), "ERRO_PAGAMENTO"));
    }
}
