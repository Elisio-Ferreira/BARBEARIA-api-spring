package br.edu.faculdade.barbearia;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ClienteService {

    private final ClienteRepository repositorio;

    public ClienteService(ClienteRepository repositorio) {
        this.repositorio = repositorio;
    }

    public List<Cliente> listar() {
        return repositorio.todos();
    }

    public Optional<Cliente> buscarPorId(String id) {
        return repositorio.porId(id);
    }

    public Cliente criar(ClienteEntrada entrada) {
        return repositorio.salvar(paraCliente(UUID.randomUUID().toString(), entrada));
    }

    // O id vem da URL, não do corpo. Repetir a chamada não muda mais nada:
    // o estado final já é o pedido (é isso que torna o PUT idempotente).
    public Optional<Cliente> trocar(String id, ClienteEntrada nova) {
        return repositorio.trocar(id, paraCliente(id, nova));
    }

    public boolean apagar(String id) {
        return repositorio.remover(id);
    }

    private Cliente paraCliente(String id, ClienteEntrada e) {
        return new Cliente(id, e.nome(), e.email(), e.telefone(), e.categoriaFavorita());
    }
}