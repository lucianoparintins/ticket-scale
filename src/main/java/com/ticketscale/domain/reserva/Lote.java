package com.ticketscale.domain.reserva;

import com.ticketscale.domain.evento.Evento;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Entity(name = "Lote")
@Table(name = "lotes")
public class Lote {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private BigDecimal preco;

    @Column(nullable = false)
    private int capacidade;

    public Lote() {}

    public Lote(UUID id, Evento evento, String nome, BigDecimal preco, int capacidade) {
        this.id = id;
        this.evento = evento;
        this.nome = nome;
        this.preco = preco;
        this.capacidade = capacidade;
    }

    public UUID getId() { return id; }
    public Evento getEvento() { return evento; }
    public String getNome() { return nome; }
    public BigDecimal getPreco() { return preco; }
    public int getCapacidade() { return capacidade; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Lote lote = (Lote) o;
        return Objects.equals(id, lote.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
