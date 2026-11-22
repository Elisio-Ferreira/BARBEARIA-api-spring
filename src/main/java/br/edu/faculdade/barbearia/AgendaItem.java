package br.edu.faculdade.barbearia;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record AgendaItem(
        String agendamentoId,
        OffsetDateTime inicio,
        OffsetDateTime fim,
        String status,
        String clienteId,
        String clienteNome,
        String clienteTelefone,
        String barbeiroId,
        String barbeiroNome,
        String servicoNome,
        BigDecimal precoCombinado
) {
}