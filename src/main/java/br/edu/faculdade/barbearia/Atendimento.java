package br.edu.faculdade.barbearia;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record Atendimento(
        String id,
        String agendamentoId,
        String clienteId,
        String clienteNome,
        String barbeiroId,
        String barbeiroNome,
        String servicoNome,
        BigDecimal valorServico,
        OffsetDateTime dataRealizacao,
        String observacoes
) {
}