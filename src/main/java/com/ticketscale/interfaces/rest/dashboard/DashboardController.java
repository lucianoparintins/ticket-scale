package com.ticketscale.interfaces.rest.dashboard;

import com.ticketscale.application.usecase.CalcularReceitaTotal;
import com.ticketscale.application.usecase.GerarRelatorioVendasPorEvento;
import com.ticketscale.application.usecase.ObterMetricasDashboard;
import com.ticketscale.domain.dashboard.FiltroDashboard;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final GerarRelatorioVendasPorEvento gerarRelatorioVendasPorEvento;
    private final CalcularReceitaTotal calcularReceitaTotal;
    private final ObterMetricasDashboard obterMetricasDashboard;

    public DashboardController(GerarRelatorioVendasPorEvento gerarRelatorioVendasPorEvento,
                               CalcularReceitaTotal calcularReceitaTotal,
                               ObterMetricasDashboard obterMetricasDashboard) {
        this.gerarRelatorioVendasPorEvento = gerarRelatorioVendasPorEvento;
        this.calcularReceitaTotal = calcularReceitaTotal;
        this.obterMetricasDashboard = obterMetricasDashboard;
    }

    @GetMapping("/vendas-por-evento")
    public ResponseEntity<List<DadosMetricaVendas>> vendasPorEvento(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim,
            @RequestParam(required = false) UUID eventoId) {

        FiltroDashboard filtro = new FiltroDashboard(dataInicio, dataFim, eventoId, 0, 100);
        var resultado = gerarRelatorioVendasPorEvento.executar(filtro);
        return ResponseEntity.ok(resultado.stream().map(DadosMetricaVendas::new).toList());
    }

    @GetMapping("/receita-total")
    public ResponseEntity<DadosRelatorioReceita> receitaTotal(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim) {

        FiltroDashboard filtro = new FiltroDashboard(dataInicio, dataFim, null, 0, 100);
        var resultado = calcularReceitaTotal.executar(filtro);
        return ResponseEntity.ok(new DadosRelatorioReceita(resultado));
    }

    @GetMapping("/metricas")
    public ResponseEntity<DadosMetricasDashboard> metricas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim) {

        FiltroDashboard filtro = new FiltroDashboard(dataInicio, dataFim, null, 0, 100);
        var resultado = obterMetricasDashboard.executar(filtro);
        return ResponseEntity.ok(new DadosMetricasDashboard(resultado));
    }
}
