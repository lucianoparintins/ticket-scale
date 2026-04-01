package com.ticketscale.interfaces.rest.reserva;

import com.ticketscale.application.reserva.LoteService;
import com.ticketscale.domain.reserva.Lote;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/lotes")
public class LoteController {

    private final LoteService service;

    public LoteController(LoteService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Lote> buscar(@PathVariable UUID id) {
        Lote lote = service.buscarPorId(id);
        return ResponseEntity.ok(lote);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Lote> atualizar(@PathVariable UUID id, @RequestBody @Valid DadosAtualizacaoLote dados) {
        Lote lote = service.atualizar(id, dados.nome(), dados.preco(), dados.capacidade());
        return ResponseEntity.ok(lote);
    }
}
