
package br.edu.faculdade.barbearia;

import java.math.BigDecimal;

public record ServicoResposta(
        String id,
        String nome,
        String descricao,
        BigDecimal preco,
        int duracaoMinutos,
        boolean ativo
) {

    public static ServicoResposta de(Servico s) {
        return new ServicoResposta(
                s.id(),
                s.nome(),
                s.descricao(),
                s.preco(),
                s.duracaoMinutos(),
                s.ativo()
        );
    }
}
