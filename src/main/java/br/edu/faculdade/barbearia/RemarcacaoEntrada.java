package br.edu.faculdade.barbearia;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record RemarcacaoEntrada(

        @NotNull(message = "O novo horário de início é obrigatório")
        LocalDateTime inicio
) {
}