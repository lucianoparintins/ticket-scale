package com.ticketscale.domain.evento;

import jakarta.persistence.*;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidade de domínio que representa um Evento.
 * Utiliza builder pattern para criação de instâncias.
 */
@Entity(name = "Evento")
@Table(name = "eventos")
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Embedded
    private PeriodoEvento periodo;

    @Column(nullable = false)
    private boolean ativo = true;

    /**
     * Construtor privado para forçar uso do builder.
     */
    private Evento(Builder builder) {
        this.id = builder.id;
        this.nome = builder.nome;
        this.descricao = builder.descricao;
        this.periodo = builder.periodo;
        this.ativo = builder.ativo;
    }

    /**
     * Construtor padrão necessário para JPA.
     */
    public Evento() {}

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public PeriodoEvento getPeriodo() {
        return periodo;
    }

    public boolean isAtivo() {
        return ativo;
    }

    /**
     * Atualiza informações do evento.
     * @param nome novo nome
     * @param descricao nova descrição
     * @param periodo novo período
     */
    public void atualizar(String nome, String descricao, PeriodoEvento periodo) {
        if (nome != null) {
            this.nome = nome;
        }
        if (descricao != null) {
            this.descricao = descricao;
        }
        if (periodo != null) {
            this.periodo = periodo;
        }
    }

    /**
     * Desativa o evento.
     */
    public void desativar() {
        this.ativo = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Evento evento = (Evento) o;
        return Objects.equals(id, evento.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    /**
     * Builder para criação de instâncias de Evento.
     * @return novo builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class para Evento.
     */
    public static class Builder {
        private UUID id;
        private String nome;
        private String descricao;
        private PeriodoEvento periodo;
        private boolean ativo = true;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder nome(String nome) {
            this.nome = nome;
            return this;
        }

        public Builder descricao(String descricao) {
            this.descricao = descricao;
            return this;
        }

        public Builder periodo(PeriodoEvento periodo) {
            this.periodo = periodo;
            return this;
        }

        public Builder ativo(boolean ativo) {
            this.ativo = ativo;
            return this;
        }

        public Evento build() {
            validar();
            return new Evento(this);
        }

        private void validar() {
            if (nome == null || nome.trim().isEmpty()) {
                throw new IllegalArgumentException("Nome é obrigatório.");
            }
            if (periodo == null) {
                throw new IllegalArgumentException("Período é obrigatório.");
            }
        }
    }
}
