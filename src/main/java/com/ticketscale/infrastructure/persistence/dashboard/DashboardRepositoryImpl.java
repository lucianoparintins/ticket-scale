package com.ticketscale.infrastructure.persistence.dashboard;

import com.ticketscale.domain.dashboard.*;
import com.ticketscale.domain.pagamento.StatusPagamento;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public class DashboardRepositoryImpl implements DashboardRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<MetricaVendas> buscarVendasPorEvento(FiltroDashboard filtro) {
        StringBuilder jpql = new StringBuilder("""
            SELECT new com.ticketscale.domain.dashboard.MetricaVendas(
                e.id, e.nome, COUNT(p.id), SUM(p.valor)
            )
            FROM Pagamento p
            JOIN Reserva r ON p.reservaId = r.id
            JOIN r.ingresso i
            JOIN i.lote l
            JOIN l.evento e
            WHERE p.status = :statusPagamento
        """);

        // Evita ":param IS NULL OR ..." pois o PostgreSQL pode nao inferir o tipo quando o valor e null.
        if (filtro.dataInicio() != null) {
            jpql.append(" AND p.dataCriacao >= :dataInicio");
        }
        if (filtro.dataFim() != null) {
            jpql.append(" AND p.dataCriacao <= :dataFim");
        }
        if (filtro.eventoId() != null) {
            jpql.append(" AND e.id = :eventoId");
        }

        jpql.append(" GROUP BY e.id, e.nome");

        TypedQuery<MetricaVendas> query = entityManager.createQuery(jpql.toString(), MetricaVendas.class);
        query.setParameter("statusPagamento", StatusPagamento.APROVADO);
        if (filtro.dataInicio() != null) {
            query.setParameter("dataInicio", filtro.dataInicio());
        }
        if (filtro.dataFim() != null) {
            query.setParameter("dataFim", filtro.dataFim());
        }
        if (filtro.eventoId() != null) {
            query.setParameter("eventoId", filtro.eventoId());
        }

        return query.getResultList();
    }

    @Override
    public RelatorioReceita calcularReceitaTotal(FiltroDashboard filtro) {
        StringBuilder jpql = new StringBuilder("""
            SELECT SUM(p.valor), COUNT(p.id)
            FROM Pagamento p
            WHERE p.status = :statusPagamento
        """);

        // Evita ":param IS NULL OR ..." pois o PostgreSQL pode nao inferir o tipo quando o valor e null.
        if (filtro.dataInicio() != null) {
            jpql.append(" AND p.dataCriacao >= :dataInicio");
        }
        if (filtro.dataFim() != null) {
            jpql.append(" AND p.dataCriacao <= :dataFim");
        }

        var query = entityManager.createQuery(jpql.toString())
                .setParameter("statusPagamento", StatusPagamento.APROVADO);

        if (filtro.dataInicio() != null) {
            query.setParameter("dataInicio", filtro.dataInicio());
        }
        if (filtro.dataFim() != null) {
            query.setParameter("dataFim", filtro.dataFim());
        }

        Object[] result = (Object[]) query.getSingleResult();

        BigDecimal total = result[0] != null ? (BigDecimal) result[0] : BigDecimal.ZERO;
        long quantidade = result[1] != null ? (long) result[1] : 0L;

        return new RelatorioReceita(total, (int) quantidade, filtro.dataInicio(), filtro.dataFim());
    }

    @Override
    public MetricasDashboard buscarMetricasDashboard(FiltroDashboard filtro) {
        RelatorioReceita receita = calcularReceitaTotal(filtro);
        List<MetricaVendas> vendasPorEvento = buscarVendasPorEvento(filtro);

        // Calcular taxa de conversão: COUNT(Pagamentos APROVADOS) / COUNT(Reservas CONFIRMADAS)
        // Na verdade, no TicketScale, uma Reserva CONFIRMADA é aquela que teve o pagamento aprovado.
        // Talvez a taxa de conversão seja Pagamentos APROVADOS / Total de Reservas Criadas.
        
        StringBuilder jpqlReservas = new StringBuilder("""
            SELECT COUNT(r.id) FROM Reserva r
            WHERE 1 = 1
        """);

        if (filtro.dataInicio() != null) {
            jpqlReservas.append(" AND r.dataCriacao >= :dataInicio");
        }
        if (filtro.dataFim() != null) {
            jpqlReservas.append(" AND r.dataCriacao <= :dataFim");
        }
        
        var queryReservas = entityManager.createQuery(jpqlReservas.toString());
        if (filtro.dataInicio() != null) {
            queryReservas.setParameter("dataInicio", filtro.dataInicio());
        }
        if (filtro.dataFim() != null) {
            queryReservas.setParameter("dataFim", filtro.dataFim());
        }

        long totalReservas = (long) queryReservas.getSingleResult();
        
        double taxaConversao = totalReservas > 0 ? (double) receita.quantidadeVendas() / totalReservas : 0.0;

        return new MetricasDashboard(
                receita.total(),
                receita.quantidadeVendas(),
                vendasPorEvento,
                taxaConversao
        );
    }
}
