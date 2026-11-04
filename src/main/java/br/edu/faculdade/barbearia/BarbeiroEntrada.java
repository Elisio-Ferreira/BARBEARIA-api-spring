
package br.edu.faculdade.barbearia;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record BarbeiroEntrada(

        @NotBlank(message = "O nome é obrigatório")
        String nome,

        @NotBlank(message = "O telefone é obrigatório")
        String telefone,

        @NotBlank(message = "O endereço é obrigatório")
        String endereco,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "O e-mail não é válido")
        String email

) {
}
