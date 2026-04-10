package com.ticketscale.interfaces.rest.evento;

import com.ticketscale.application.evento.EventoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/eventos")
public class EventoController {

    private final EventoService service;

    public EventoController(EventoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<DadosDetalhamentoEvento> criar(@RequestBody @Valid DadosCadastroEvento dados, UriComponentsBuilder uriBuilder) {
        var evento = service.criar(dados.nome(), dados.descricao(), dados.dataInicio(), dados.dataFim());
        var uri = uriBuilder.path("/eventos/{id}").buildAndExpand(evento.getId()).toUri();
        return ResponseEntity.created(uri).body(new DadosDetalhamentoEvento(evento));
    }

    @GetMapping
    public ResponseEntity<List<DadosDetalhamentoEvento>> listar() {
        var eventos = service.listarAtivos().stream().map(DadosDetalhamentoEvento::new).toList();
        return ResponseEntity.ok(eventos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DadosDetalhamentoEvento> detalhar(@PathVariable UUID id) {
        var evento = service.buscarPorId(id);
        return ResponseEntity.ok(new DadosDetalhamentoEvento(evento));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativar(@PathVariable UUID id) {
        service.desativar(id);
        return ResponseEntity.noContent().build();
    }
}
