package com.ticketscale.application.usecase;

import com.ticketscale.domain.dashboard.DashboardRepository;
import com.ticketscale.domain.dashboard.FiltroDashboard;
import com.ticketscale.domain.dashboard.MetricasDashboard;
import com.ticketscale.infrastructure.config.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ObterMetricasDashboard {

    private final DashboardRepository dashboardRepository;

    public ObterMetricasDashboard(DashboardRepository dashboardRepository) {
        this.dashboardRepository = dashboardRepository;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CACHE_DASHBOARD, key = "#filtro")
    public MetricasDashboard executar(FiltroDashboard filtro) {
        return dashboardRepository.buscarMetricasDashboard(filtro);
    }
}
