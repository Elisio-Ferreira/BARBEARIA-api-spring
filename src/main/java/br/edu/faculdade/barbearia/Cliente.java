package br.edu.faculdade.barbearia;

/**
 * Frente 3 · quem é atendido na barbearia.
 *
 * Nenhum campo de senha, de propósito: guardar senha exige cuidados que ainda
 * não fazem parte desta etapa. Quando ela entrar, GET /clientes deixa de ser
 * público.
 */
public record Cliente(
        String id,
        String nome,
        String email,
        String telefone,
        String categoriaFavorita
) { }
