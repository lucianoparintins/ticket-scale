package com.ticketscale.domain.pagamento;

import java.io.Serializable;

public sealed interface DadosMetodoPagamento extends Serializable
    permits DadosPix, DadosCartao {}
