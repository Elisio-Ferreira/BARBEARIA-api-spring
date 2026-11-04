
package br.edu.faculdade.barbearia;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    public boolean desativar(String id) {
        return repositorio.desativar(id);
    }
}
