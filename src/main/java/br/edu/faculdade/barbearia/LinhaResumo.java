package br.edu.faculdade.barbearia;

import java.math.BigDecimal;

public record LinhaResumo(
        String chave,
        String nome,
        long quantidade,
        BigDecimal valor
) {
}