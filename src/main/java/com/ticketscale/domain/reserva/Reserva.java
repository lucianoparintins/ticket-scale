package com.ticketscale.domain.reserva;

import com.ticketscale.domain.usuario.Usuario;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity(name = "Reserva")
@Table(name = "reservas")
public class Reserva {

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

    public Reserva() {}

    public Reserva(Usuario usuario, Ingresso ingresso) {
        this(null, usuario, ingresso);
    }

    public Reserva(UUID id, Usuario usuario, Ingresso ingresso) {
        this.id = id;
        this.usuario = usuario;
        this.ingresso = ingresso;
        this.status = StatusReserva.PENDENTE;
        this.dataCriacao = LocalDateTime.now();
        // Conforme definido na aprovação, a expiração é em 5 minutos
        this.dataExpiracao = this.dataCriacao.plusMinutes(5);
    }

    public UUID getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public Ingresso getIngresso() { return ingresso; }
    public StatusReserva getStatus() { return status; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public LocalDateTime getDataExpiracao() { return dataExpiracao; }

    public void confirmarPagamento() {
        if (this.status != StatusReserva.PENDENTE) {
            throw new IllegalStateException("Apenas reservas pendentes podem ser confirmadas.");
        }
        this.status = StatusReserva.CONFIRMADA;
        this.ingresso.vender();
    }

    public void cancelar() {
        if (this.status == StatusReserva.CONFIRMADA) {
            throw new IllegalStateException("Reserva já confirmada não pode ser cancelada diretamente.");
        }
        this.status = StatusReserva.CANCELADA;
        this.ingresso.liberar();
    }

    public boolean isExpirada() {
        return LocalDateTime.now().isAfter(dataExpiracao) && this.status == StatusReserva.PENDENTE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reserva reserva = (Reserva) o;
        return Objects.equals(id, reserva.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
