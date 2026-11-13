package br.edu.faculdade.barbearia;

import jakarta.validation.constraints.Size;

public record CancelamentoEntrada(

        @Size(max = 500, message = "O motivo pode ter no máximo 500 caracteres")
        String motivo
) {
}