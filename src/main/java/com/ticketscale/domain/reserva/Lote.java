package com.ticketscale.domain.reserva;

import com.ticketscale.domain.evento.Evento;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidade de domínio que representa um Lote de ingressos.
 * Utiliza builder pattern para criação de instâncias.
 */
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

    /**
     * Construtor privado para forçar uso do builder.
     */
    private Lote(Builder builder) {
        this.id = builder.id;
        this.evento = builder.evento;
        this.nome = builder.nome;
        this.preco = builder.preco;
        this.capacidade = builder.capacidade;
    }

    /**
     * Construtor padrão necessário para JPA.
     */
    public Lote() {}

    public UUID getId() {
        return id;
    }

    public Evento getEvento() {
        return evento;
    }

    public String getNome() {
        return nome;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public int getCapacidade() {
        return capacidade;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Lote lote = (Lote) o;
        return Objects.equals(id, lote.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    /**
     * Builder para criação de instâncias de Lote.
     * @return novo builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class para Lote.
     */
    public static class Builder {
        private UUID id;
        private Evento evento;
        private String nome;
        private BigDecimal preco;
        private int capacidade;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder evento(Evento evento) {
            this.evento = evento;
            return this;
        }

        public Builder nome(String nome) {
            this.nome = nome;
            return this;
        }

        public Builder preco(BigDecimal preco) {
            this.preco = preco;
            return this;
        }

        public Builder capacidade(int capacidade) {
            this.capacidade = capacidade;
            return this;
        }

        public Lote build() {
            validar();
            return new Lote(this);
        }

        private void validar() {
            if (evento == null) {
                throw new IllegalArgumentException("Evento é obrigatório.");
            }
            if (nome == null || nome.trim().isEmpty()) {
                throw new IllegalArgumentException("Nome é obrigatório.");
            }
            if (preco == null || preco.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Preço deve ser maior ou igual a zero.");
            }
            if (capacidade <= 0) {
                throw new IllegalArgumentException("Capacidade deve ser maior que zero.");
            }
        }
    }
}
