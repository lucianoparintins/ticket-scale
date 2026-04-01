package com.ticketscale.interfaces.rest.reserva;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;

public record DadosAtualizacaoLote(
    String nome,
    
    @DecimalMin("0.0")
    BigDecimal preco,
    
    @Min(1)
    Integer capacidade
) {
}
