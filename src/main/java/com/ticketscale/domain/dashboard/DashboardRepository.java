package com.ticketscale.domain.dashboard;

import java.util.List;

public interface DashboardRepository {
    List<MetricaVendas> buscarVendasPorEvento(FiltroDashboard filtro);
    RelatorioReceita calcularReceitaTotal(FiltroDashboard filtro);
    MetricasDashboard buscarMetricasDashboard(FiltroDashboard filtro);
}
