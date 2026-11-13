package br.edu.faculdade.barbearia;

import java.time.LocalDateTime;

public record HorarioDisponivel(
        LocalDateTime inicio,
        LocalDateTime fim
) {
}