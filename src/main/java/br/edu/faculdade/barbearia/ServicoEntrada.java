package br.edu.faculdade.barbearia;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Frente 1 · o contrato de ENTRADA: o que o cliente manda no POST e no PUT.
 *
 * Não tem id: quem inventa o id é a API.
 * As anotações só valem porque o controller usa @Valid.
 *
 * Double, Integer e Boolean (objetos, e não primitivos) de propósito: com o
 * primitivo, um campo ausente viraria 0/false sem ninguém perceber. Com o
 * objeto, "não mandou" chega como null e o @NotNull consegue reclamar.
 */
public record ServicoEntrada(
        @NotBlank(message = "o nome é obrigatório")
        @Size(max = 80, message = "o nome pode ter no máximo 80 caracteres")
        String nome,

        @NotBlank(message = "a categoria é obrigatória")
        String categoria,

        @NotNull(message = "o preço é obrigatório")
        @Positive(message = "o preço deve ser maior que zero")
        Double preco,

        @NotNull(message = "a duração é obrigatória")
        @Positive(message = "a duração deve ser maior que zero")
        Integer duracaoMinutos,

        @NotBlank(message = "o barbeiro é obrigatório")
        String barbeiroSlug,

        String descricao,

        Boolean promocao
) { }
