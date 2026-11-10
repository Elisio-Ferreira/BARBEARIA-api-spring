
package br.edu.faculdade.barbearia;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthEntrada(

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "O e-mail não é válido")
        String email,

        @NotBlank(message = "A senha é obrigatória")
        String senha

) {
}
