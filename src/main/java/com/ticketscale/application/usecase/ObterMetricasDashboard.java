package com.ticketscale.application.usecase;

import com.ticketscale.domain.dashboard.DashboardRepository;
import com.ticketscale.domain.dashboard.FiltroDashboard;
import com.ticketscale.domain.dashboard.MetricasDashboard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ObterMetricasDashboard {

    private final DashboardRepository dashboardRepository;

    public ObterMetricasDashboard(DashboardRepository dashboardRepository) {
        this.dashboardRepository = dashboardRepository;
    }

    @Transactional(readOnly = true)
    public MetricasDashboard executar(FiltroDashboard filtro) {
        return dashboardRepository.buscarMetricasDashboard(filtro);
    }
}
