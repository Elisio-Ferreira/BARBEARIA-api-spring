package br.edu.faculdade.barbearia;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record Agendamento(
        String id,
        String clienteId,
        String barbeiroId,
        String servicoId,
        OffsetDateTime inicio,
        OffsetDateTime fim,
        BigDecimal precoCombinado,
        int duracaoMinutos,
        String status,
        String motivoCancelamento
) {
}