package br.edu.faculdade.barbearia;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Frente 2 · o que o cliente manda no POST /barbeiros.
 *
 * O slug vem do cliente, então precisa de formato: só minúsculas, números e
 * hífens, sem espaço nem acento, porque ele vira parte do endereço.
 */
public record BarbeiroEntrada(
        @NotBlank(message = "o nome é obrigatório")
        String nome,

        @NotBlank(message = "o slug é obrigatório")
        @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$",
                message = "o slug deve ter só letras minúsculas, números e hífens (ex.: joao-navalha)")
        String slug,

        @NotBlank(message = "a especialidade é obrigatória")
        String especialidade,

        String descricao
) { }