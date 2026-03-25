package com.ticketscale.domain.reserva;

import jakarta.persistence.*;
import java.util.Objects;
import java.util.UUID;

@Entity(name = "Ingresso")
@Table(name = "ingressos")
public class Ingresso {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id", nullable = false)
    private Lote lote;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusIngresso status;

    public Ingresso() {}

    public Ingresso(UUID id, Lote lote, StatusIngresso status) {
        this.id = id;
        this.lote = lote;
        this.status = status;
    }

    public UUID getId() { return id; }
    public Lote getLote() { return lote; }
    public StatusIngresso getStatus() { return status; }

    public void reservar() {
        if (this.status != StatusIngresso.LIVRE) {
            throw new IllegalStateException("Ingresso não está livre para reserva.");
        }
        this.status = StatusIngresso.RESERVADO;
    }

    public void vender() {
        if (this.status != StatusIngresso.RESERVADO) {
            throw new IllegalStateException("Apenas ingressos reservados podem ser vendidos.");
        }
        this.status = StatusIngresso.VENDIDO;
    }

    public void liberar() {
        this.status = StatusIngresso.LIVRE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ingresso ingresso = (Ingresso) o;
        return Objects.equals(id, ingresso.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
