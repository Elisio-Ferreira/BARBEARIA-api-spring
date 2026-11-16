package br.edu.faculdade.barbearia;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record Pagamento(
        String id,
        String agendamentoId,
        BigDecimal valor,
        String formaPagamento,
        String status,
        OffsetDateTime dataPagamento,
        String referencia
) {
}