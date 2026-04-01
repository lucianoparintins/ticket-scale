package com.ticketscale.application.usecase;

import com.ticketscale.domain.dashboard.DashboardRepository;
import com.ticketscale.domain.dashboard.FiltroDashboard;
import com.ticketscale.domain.dashboard.RelatorioReceita;
import com.ticketscale.infrastructure.config.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CalcularReceitaTotal {

    private final DashboardRepository dashboardRepository;

    public CalcularReceitaTotal(DashboardRepository dashboardRepository) {
        this.dashboardRepository = dashboardRepository;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CACHE_DASHBOARD, key = "'receita_total:' + #filtro")
    public RelatorioReceita executar(FiltroDashboard filtro) {
        return dashboardRepository.calcularReceitaTotal(filtro);
    }
}
