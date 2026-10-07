package br.edu.faculdade.barbearia;

/**
 * Frente 2 · quem atende na barbearia.
 *
 * O identificador desta frente é o SLUG, não o id: é ele que vai na URL
 * (/barbeiros/marcos-fade), é ele que o Servico guarda em barbeiroSlug, e é
 * ele que o cliente escolhe ao criar. O id fica só como identificador interno.
 */
public record Barbeiro(
        String id,
        String nome,
        String slug,
        String especialidade,
        String descricao
) { }