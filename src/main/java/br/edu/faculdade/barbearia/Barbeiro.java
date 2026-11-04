
package br.edu.faculdade.barbearia;

public record Barbeiro(
        String id,
        String perfilId,
        String nome,
        String telefone,
        String endereco,
        String email,
        boolean ativo
) {
}
