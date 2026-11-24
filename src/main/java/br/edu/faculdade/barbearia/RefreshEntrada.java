package br.edu.faculdade.barbearia;

import jakarta.validation.constraints.NotBlank;

public record RefreshEntrada(

        @NotBlank(message = "O refreshToken é obrigatório")
        String refreshToken
) {
}