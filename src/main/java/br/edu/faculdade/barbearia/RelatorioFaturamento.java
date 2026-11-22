package br.edu.faculdade.barbearia;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RelatorioFaturamento(
        LocalDate de,
        LocalDate ate,
        long quantidadePagamentos,
        BigDecimal totalRecebido,
        BigDecimal totalEstornado,
        List<LinhaResumo> porFormaPagamento,
        List<LinhaResumo> porBarbeiro
) {
}