package com.ticketscale.interfaces.rest;

import com.ticketscale.application.usecase.ReservarIngressoUseCase;
import com.ticketscale.domain.reserva.Reserva;
import com.ticketscale.interfaces.rest.dto.ReservaRequestDTO;
import com.ticketscale.interfaces.rest.dto.ReservaResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reservas")
public class ReservaController {

    private final ReservarIngressoUseCase reservarIngressoUseCase;

    public ReservaController(ReservarIngressoUseCase reservarIngressoUseCase) {
        this.reservarIngressoUseCase = reservarIngressoUseCase;
    }

    @PostMapping
    public ResponseEntity<ReservaResponseDTO> reservar(@RequestBody @Valid ReservaRequestDTO request) {
        Reserva reserva = reservarIngressoUseCase.executar(request.loteId(), request.usuarioId());
        
        ReservaResponseDTO response = new ReservaResponseDTO(
            reserva.getId(),
            reserva.getUsuario().getId(),
            reserva.getIngresso().getId(),
            reserva.getStatus(),
            reserva.getDataCriacao(),
            reserva.getDataExpiracao()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
