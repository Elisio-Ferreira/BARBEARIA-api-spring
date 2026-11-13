package br.edu.faculdade.barbearia;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record HorarioTrabalhoEntrada(

        @NotNull(message = "O dia da semana é obrigatório")
        @Min(value = 0, message = "O dia da semana vai de 0 (domingo) a 6 (sábado)")
        @Max(value = 6, message = "O dia da semana vai de 0 (domingo) a 6 (sábado)")
        Integer diaSemana,

        @NotNull(message = "A hora de início é obrigatória")
        LocalTime horaInicio,

        @NotNull(message = "A hora de fim é obrigatória")
        LocalTime horaFim
) {
}