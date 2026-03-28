package com.ticketscale.infrastructure.persistence.dashboard;

import com.ticketscale.domain.dashboard.*;
import com.ticketscale.domain.pagamento.StatusPagamento;
import com.ticketscale.domain.reserva.StatusReserva;
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
        String jpql = """
            SELECT new com.ticketscale.domain.dashboard.MetricaVendas(
                e.id, e.nome, COUNT(p.id), SUM(p.valor)
            )
            FROM Pagamento p
            JOIN Reserva r ON p.reservaId = r.id
            JOIN r.ingresso i
            JOIN i.lote l
            JOIN l.evento e
            WHERE p.status = :statusPagamento
              AND (:dataInicio IS NULL OR p.dataCriacao >= :dataInicio)
              AND (:dataFim IS NULL OR p.dataCriacao <= :dataFim)
              AND (:eventoId IS NULL OR e.id = :eventoId)
            GROUP BY e.id, e.nome
        """;

        TypedQuery<MetricaVendas> query = entityManager.createQuery(jpql, MetricaVendas.class);
        query.setParameter("statusPagamento", StatusPagamento.APROVADO);
        query.setParameter("dataInicio", filtro.dataInicio());
        query.setParameter("dataFim", filtro.dataFim());
        query.setParameter("eventoId", filtro.eventoId());

        return query.getResultList();
    }

    @Override
    public RelatorioReceita calcularReceitaTotal(FiltroDashboard filtro) {
        String jpql = """
            SELECT SUM(p.valor), COUNT(p.id)
            FROM Pagamento p
            WHERE p.status = :statusPagamento
              AND (:dataInicio IS NULL OR p.dataCriacao >= :dataInicio)
              AND (:dataFim IS NULL OR p.dataCriacao <= :dataFim)
        """;

        Object[] result = (Object[]) entityManager.createQuery(jpql)
                .setParameter("statusPagamento", StatusPagamento.APROVADO)
                .setParameter("dataInicio", filtro.dataInicio())
                .setParameter("dataFim", filtro.dataFim())
                .getSingleResult();

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
        
        String jpqlReservas = """
            SELECT COUNT(r.id) FROM Reserva r
            WHERE (:dataInicio IS NULL OR r.dataCriacao >= :dataInicio)
              AND (:dataFim IS NULL OR r.dataCriacao <= :dataFim)
        """;
        
        long totalReservas = (long) entityManager.createQuery(jpqlReservas)
                .setParameter("dataInicio", filtro.dataInicio())
                .setParameter("dataFim", filtro.dataFim())
                .getSingleResult();
        
        double taxaConversao = totalReservas > 0 ? (double) receita.quantidadeVendas() / totalReservas : 0.0;

        return new MetricasDashboard(
                receita.total(),
                receita.quantidadeVendas(),
                vendasPorEvento,
                taxaConversao
        );
    }
}
