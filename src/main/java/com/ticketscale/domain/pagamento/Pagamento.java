package com.ticketscale.domain.pagamento;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity(name = "Pagamento")
@Table(name = "pagamentos")
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "reserva_id", nullable = false)
    private UUID reservaId;

    @Column(nullable = false)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPagamento status;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pagamento", nullable = false)
    private MetodoPagamento metodoPagamento;

    @Column(name = "transacao_externa_id")
    private String transacaoExternaId;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_atualizacao", nullable = false)
    private LocalDateTime dataAtualizacao;

    public Pagamento() {}

    public Pagamento(UUID reservaId, BigDecimal valor, MetodoPagamento metodoPagamento) {
        this();
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor do pagamento deve ser maior que zero.");
        }
        if (reservaId == null) {
            throw new IllegalArgumentException("ID da reserva é obrigatório.");
        }
        if (metodoPagamento == null) {
            throw new IllegalArgumentException("Método de pagamento é obrigatório.");
        }
        this.reservaId = reservaId;
        this.valor = valor;
        this.metodoPagamento = metodoPagamento;
        this.status = StatusPagamento.PENDENTE;
        this.dataCriacao = LocalDateTime.now();
        this.dataAtualizacao = this.dataCriacao;
    }

    private Pagamento(Builder builder) {
        this.id = builder.id;
        this.reservaId = builder.reservaId;
        this.valor = builder.valor;
        this.status = builder.status;
        this.metodoPagamento = builder.metodoPagamento;
        this.transacaoExternaId = builder.transacaoExternaId;
        this.dataCriacao = builder.dataCriacao;
        this.dataAtualizacao = builder.dataAtualizacao;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UUID reservaId;
        private BigDecimal valor;
        private StatusPagamento status = StatusPagamento.PENDENTE;
        private MetodoPagamento metodoPagamento;
        private String transacaoExternaId;
        private LocalDateTime dataCriacao;
        private LocalDateTime dataAtualizacao;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder reservaId(UUID reservaId) { this.reservaId = reservaId; return this; }
        public Builder valor(BigDecimal valor) { this.valor = valor; return this; }
        public Builder status(StatusPagamento status) { this.status = status; return this; }
        public Builder metodoPagamento(MetodoPagamento metodoPagamento) { this.metodoPagamento = metodoPagamento; return this; }
        public Builder transacaoExternaId(String transacaoExternaId) { this.transacaoExternaId = transacaoExternaId; return this; }
        public Builder dataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; return this; }
        public Builder dataAtualizacao(LocalDateTime dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; return this; }

        public Pagamento build() {
            if (dataCriacao == null) dataCriacao = LocalDateTime.now();
            if (dataAtualizacao == null) dataAtualizacao = dataCriacao;
            return new Pagamento(this);
        }
    }

    public void confirmar(String transacaoExternaId) {
        if (this.status != StatusPagamento.PENDENTE) {
            throw new PagamentoException("Apenas pagamentos pendentes podem ser confirmados.");
        }
        this.status = StatusPagamento.APROVADO;
        this.transacaoExternaId = transacaoExternaId;
        this.dataAtualizacao = LocalDateTime.now();
    }

    public void recusar() {
        if (this.status != StatusPagamento.PENDENTE) {
            throw new PagamentoException("Apenas pagamentos pendentes podem ser recusados.");
        }
        this.status = StatusPagamento.RECUSADO;
        this.dataAtualizacao = LocalDateTime.now();
    }

    public boolean isAprovado() {
        return this.status == StatusPagamento.APROVADO;
    }

    public UUID getId() {
        return id;
    }

    public UUID getReservaId() {
        return reservaId;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public StatusPagamento getStatus() {
        return status;
    }

    public MetodoPagamento getMetodoPagamento() {
        return metodoPagamento;
    }

    public String getTransacaoExternaId() {
        return transacaoExternaId;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pagamento pagamento = (Pagamento) o;
        return Objects.equals(id, pagamento.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
