
package br.edu.faculdade.barbearia;

public record Cliente(
        String id,
        String perfilId,
        String nome,
        String email,
        String telefone,
        String endereco,
        boolean ativo
) {
}
