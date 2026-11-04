
package br.edu.faculdade.barbearia;

import java.math.BigDecimal;

public record Servico(
        String id,
        String nome,
        String descricao,
        BigDecimal preco,
        int duracaoMinutos,
        boolean ativo
) {
}
