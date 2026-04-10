package com.ticketscale.application.usecase;

import com.ticketscale.domain.dashboard.DashboardRepository;
import com.ticketscale.domain.dashboard.FiltroDashboard;
import com.ticketscale.domain.dashboard.MetricaVendas;
import com.ticketscale.infrastructure.config.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GerarRelatorioVendasPorEvento {

    private final DashboardRepository dashboardRepository;

    public GerarRelatorioVendasPorEvento(DashboardRepository dashboardRepository) {
        this.dashboardRepository = dashboardRepository;
    }

    @Transactional(readOnly = true)
    @Cacheable(
            value = CacheConfig.CACHE_DASHBOARD,
            key = "'vendas_evento:' + #filtro",
            unless = "#result == null || #result.isEmpty()"
    )
    public List<MetricaVendas> executar(FiltroDashboard filtro) {
        return dashboardRepository.buscarVendasPorEvento(filtro);
    }
}
