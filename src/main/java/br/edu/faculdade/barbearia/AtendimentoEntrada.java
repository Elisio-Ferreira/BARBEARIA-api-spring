package br.edu.faculdade.barbearia;

import jakarta.validation.constraints.Size;

public record AtendimentoEntrada(

        @Size(max = 1000, message = "As observações podem ter no máximo 1000 caracteres")
        String observacoes
) {
}