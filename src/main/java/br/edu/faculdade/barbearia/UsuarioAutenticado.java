package br.edu.faculdade.barbearia;

public record UsuarioAutenticado(
        String perfilId,
        String nome,
        String email,
        String papel,
        boolean ativo,
        String clienteId,
        String barbeiroId
) {
}