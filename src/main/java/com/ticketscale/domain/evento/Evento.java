package com.ticketscale.domain.evento;

import jakarta.persistence.*;
import java.util.Objects;
import java.util.UUID;

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

    private boolean ativo = true;

    public Evento() {}

    public Evento(UUID id, String nome, String descricao, PeriodoEvento periodo) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.periodo = periodo;
        this.ativo = true;
    }

    public UUID getId() { return id; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public PeriodoEvento getPeriodo() { return periodo; }
    public boolean isAtivo() { return ativo; }

    public void atualizar(String nome, String descricao, PeriodoEvento periodo) {
        if (nome != null) this.nome = nome;
        if (descricao != null) this.descricao = descricao;
        if (periodo != null) this.periodo = periodo;
    }

    public void desativar() {
        this.ativo = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Evento evento = (Evento) o;
        return Objects.equals(id, evento.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
