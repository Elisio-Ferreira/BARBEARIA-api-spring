package br.edu.faculdade.barbearia;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RelatorioAtendimentos(
        LocalDate de,
        LocalDate ate,
        long totalAtendimentos,
        BigDecimal valorTotal,
        List<LinhaResumo> porBarbeiro,
        List<LinhaResumo> porServico
) {
}