package br.edu.faculdade.barbearia;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record HorariosTrabalhoEntrada(

        @NotNull(message = "A lista de horários é obrigatória")
        @Valid
        List<HorarioTrabalhoEntrada> horarios
) {
}