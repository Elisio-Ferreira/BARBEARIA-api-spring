package br.edu.faculdade.barbearia;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record BloqueioEntrada(

        @NotNull(message = "O início é obrigatório")
        LocalDateTime inicio,

        @NotNull(message = "O fim é obrigatório")
        LocalDateTime fim,

        @Size(max = 100, message = "O motivo pode ter no máximo 100 caracteres")
        String motivo
) {
}