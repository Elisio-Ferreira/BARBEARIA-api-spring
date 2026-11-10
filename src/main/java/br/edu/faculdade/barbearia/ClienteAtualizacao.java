package br.edu.faculdade.barbearia;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClienteAtualizacao(

        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 100, message = "O nome pode ter no máximo 100 caracteres")
        String nome,

        @NotBlank(message = "O telefone é obrigatório")
        String telefone,

        String endereco
) {
}