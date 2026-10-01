package br.edu.faculdade.barbearia;

/**
 * Frente 1 · o contrato de SAÍDA: o que a API devolve.
 *
 * Tem id (o cliente precisa dele para chamar /servicos/{id}) e NÃO tem
 * descricao: a listagem não precisa do texto longo.
 *
 * O método de é a única tradução de Servico para ServicoResposta. Ele é
 * público porque a frente 4 (busca) também devolve serviços.
 */
public record ServicoResposta(
        String id,
        String nome,
        String categoria,
        double preco,
        int duracaoMinutos,
        boolean promocao,
        String barbeiroSlug
) {
    public static ServicoResposta de(Servico s) {
        return new ServicoResposta(s.id(), s.nome(), s.categoria(), s.preco(),
                s.duracaoMinutos(), s.promocao(), s.barbeiroSlug());
    }
}
