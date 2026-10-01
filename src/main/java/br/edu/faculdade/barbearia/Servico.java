package br.edu.faculdade.barbearia;

/**
 * Frente 1 · o modelo do recurso principal: um serviço que a barbearia oferece.
 *
 * Record é só dado, então não leva anotação (@Service, @Repository etc. marcam
 * classes que FAZEM algo; o record apenas guarda).
 *
 * O id é String porque o que vem da URL é sempre texto.
 *
 * Este é o modelo "do lado de dentro": vai do service para o repository. O que
 * o cliente manda é ServicoEntrada; o que a API devolve é ServicoResposta.
 * Por isso a descricao existe aqui mas não aparece na listagem.
 */
public record Servico(
        String id,
        String nome,
        String descricao,
        String categoria,
        double preco,
        int duracaoMinutos,
        boolean promocao,
        String barbeiroSlug
) { }
