package br.edu.faculdade.barbearia;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BarbeiroCadastroEntrada(

        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 100, message = "O nome pode ter no máximo 100 caracteres")
        String nome,

        @NotBlank(message = "O telefone é obrigatório")
        String telefone,

        String endereco,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "O e-mail não é válido")
        String email,

        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
        String senha
) {
}