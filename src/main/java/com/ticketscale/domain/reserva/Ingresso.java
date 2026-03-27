package com.ticketscale.domain.reserva;

import jakarta.persistence.*;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidade de domínio que representa um Ingresso.
 * Utiliza builder pattern para criação de instâncias.
 */
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

    /**
     * Construtor privado para forçar uso do builder.
     */
    private Ingresso(Builder builder) {
        this.id = builder.id;
        this.lote = builder.lote;
        this.status = builder.status;
    }

    /**
     * Construtor padrão necessário para JPA.
     */
    public Ingresso() {}

    public UUID getId() {
        return id;
    }

    public Lote getLote() {
        return lote;
    }

    public StatusIngresso getStatus() {
        return status;
    }

    /**
     * Reserva o ingresso.
     * @throws IllegalStateException se ingresso não estiver livre
     */
    public void reservar() {
        if (this.status != StatusIngresso.LIVRE) {
            throw new IllegalStateException("Ingresso não está livre para reserva.");
        }
        this.status = StatusIngresso.RESERVADO;
    }

    /**
     * Vende o ingresso (após reserva confirmada).
     * @throws IllegalStateException se ingresso não estiver reservado
     */
    public void vender() {
        if (this.status != StatusIngresso.RESERVADO) {
            throw new IllegalStateException("Apenas ingressos reservados podem ser vendidos.");
        }
        this.status = StatusIngresso.VENDIDO;
    }

    /**
     * Libera o ingresso (cancela reserva).
     */
    public void liberar() {
        this.status = StatusIngresso.LIVRE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Ingresso ingresso = (Ingresso) o;
        return Objects.equals(id, ingresso.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    /**
     * Builder para criação de instâncias de Ingresso.
     * @return novo builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class para Ingresso.
     */
    public static class Builder {
        private UUID id;
        private Lote lote;
        private StatusIngresso status = StatusIngresso.LIVRE;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder lote(Lote lote) {
            this.lote = lote;
            return this;
        }

        public Builder status(StatusIngresso status) {
            this.status = status;
            return this;
        }

        public Ingresso build() {
            validar();
            return new Ingresso(this);
        }

        private void validar() {
            if (lote == null) {
                throw new IllegalArgumentException("Lote é obrigatório.");
            }
        }
    }
}
