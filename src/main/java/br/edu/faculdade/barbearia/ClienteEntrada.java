package br.edu.faculdade.barbearia;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Frente 3 · o que o cliente da API manda no POST e no PUT de /clientes.
 * Sem id: quem inventa é a API.
 */
public record ClienteEntrada(
        @NotBlank(message = "o nome é obrigatório")
        String nome,

        @NotBlank(message = "o e-mail é obrigatório")
        @Email(message = "o e-mail não é válido")
        String email,

        String telefone,

        String categoriaFavorita
) { }
