package br.edu.faculdade.barbearia;

/**
 * Frente 2 · lançada quando alguém tenta criar um barbeiro com um slug que já
 * está em uso. O TratadorDeErros a traduz em 409 Conflict.
 */
public class BarbeiroJaExisteException extends RuntimeException {

    public BarbeiroJaExisteException(String slug) {
        super("Já existe um barbeiro com o slug '" + slug + "'.");
    }
}