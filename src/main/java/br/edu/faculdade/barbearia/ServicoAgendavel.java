package br.edu.faculdade.barbearia;

import java.math.BigDecimal;

public record ServicoAgendavel(
        String id,
        String nome,
        BigDecimal preco,
        int duracaoMinutos,
        boolean ativo
) {
}