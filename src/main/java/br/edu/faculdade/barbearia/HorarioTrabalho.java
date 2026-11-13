package br.edu.faculdade.barbearia;

import java.time.LocalTime;

public record HorarioTrabalho(
        String id,
        int diaSemana,
        LocalTime horaInicio,
        LocalTime horaFim,
        boolean ativo
) {
}