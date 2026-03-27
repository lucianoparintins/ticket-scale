package com.ticketscale.domain.reserva;

import com.ticketscale.domain.usuario.Usuario;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidade de domínio que representa uma Reserva de ingresso.
 * Utiliza builder pattern para criação de instâncias.
 */
@Entity(name = "Reserva")
@Table(name = "reservas")
public class Reserva {

    private static final int MINUTOS_EXPIRACAO = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingresso_id", nullable = false)
    private Ingresso ingresso;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusReserva status;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_expiracao", nullable = false)
    private LocalDateTime dataExpiracao;

    /**
     * Construtor privado para forçar uso do builder.
     */
    private Reserva(Builder builder) {
        this.id = builder.id;
        this.usuario = builder.usuario;
        this.ingresso = builder.ingresso;
        this.status = builder.status;
        this.dataCriacao = builder.dataCriacao;
        this.dataExpiracao = builder.dataExpiracao;
    }

    /**
     * Construtor padrão necessário para JPA.
     */
    public Reserva() {}

    /**
     * Cria uma nova reserva com usuário e ingresso.
     * @param usuario usuário da reserva
     * @param ingresso ingresso reservado
     */
    public Reserva(Usuario usuario, Ingresso ingresso) {
        this.usuario = usuario;
        this.ingresso = ingresso;
        this.status = StatusReserva.PENDENTE;
        this.dataCriacao = LocalDateTime.now();
        this.dataExpiracao = this.dataCriacao.plusMinutes(MINUTOS_EXPIRACAO);
    }

    public UUID getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Ingresso getIngresso() {
        return ingresso;
    }

    public StatusReserva getStatus() {
        return status;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public LocalDateTime getDataExpiracao() {
        return dataExpiracao;
    }

    /**
     * Setter protegido para uso do JPA.
     */
    protected void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    /**
     * Setter protegido para uso do JPA.
     */
    protected void setDataExpiracao(LocalDateTime dataExpiracao) {
        this.dataExpiracao = dataExpiracao;
    }

    /**
     * Confirma o pagamento da reserva.
     * @throws IllegalStateException se reserva não estiver pendente
     */
    public void confirmarPagamento() {
        if (this.status != StatusReserva.PENDENTE) {
            throw new IllegalStateException("Apenas reservas pendentes podem ser confirmadas.");
        }
        this.status = StatusReserva.CONFIRMADA;
        this.ingresso.vender();
    }

    /**
     * Cancela a reserva.
     * @throws IllegalStateException se reserva já estiver confirmada
     */
    public void cancelar() {
        if (this.status == StatusReserva.CONFIRMADA) {
            throw new IllegalStateException("Reserva já confirmada não pode ser cancelada diretamente.");
        }
        this.status = StatusReserva.CANCELADA;
        this.ingresso.liberar();
    }

    /**
     * Verifica se a reserva está expirada.
     * @return true se expirada
     */
    public boolean isExpirada() {
        return LocalDateTime.now().isAfter(dataExpiracao) && this.status == StatusReserva.PENDENTE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Reserva reserva = (Reserva) o;
        return Objects.equals(id, reserva.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    /**
     * Builder para criação de instâncias de Reserva.
     * @return novo builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class para Reserva.
     */
    public static class Builder {
        private UUID id;
        private Usuario usuario;
        private Ingresso ingresso;
        private StatusReserva status = StatusReserva.PENDENTE;
        private LocalDateTime dataCriacao;
        private LocalDateTime dataExpiracao;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder usuario(Usuario usuario) {
            this.usuario = usuario;
            return this;
        }

        public Builder ingresso(Ingresso ingresso) {
            this.ingresso = ingresso;
            return this;
        }

        public Builder status(StatusReserva status) {
            this.status = status;
            return this;
        }

        public Builder dataCriacao(LocalDateTime dataCriacao) {
            this.dataCriacao = dataCriacao;
            return this;
        }

        public Builder dataExpiracao(LocalDateTime dataExpiracao) {
            this.dataExpiracao = dataExpiracao;
            return this;
        }

        public Reserva build() {
            validar();
            if (dataCriacao == null) {
                dataCriacao = LocalDateTime.now();
            }
            if (dataExpiracao == null) {
                dataExpiracao = dataCriacao.plusMinutes(MINUTOS_EXPIRACAO);
            }
            return new Reserva(this);
        }

        private void validar() {
            if (usuario == null) {
                throw new IllegalArgumentException("Usuário é obrigatório.");
            }
            if (ingresso == null) {
                throw new IllegalArgumentException("Ingresso é obrigatório.");
            }
        }
    }
}
