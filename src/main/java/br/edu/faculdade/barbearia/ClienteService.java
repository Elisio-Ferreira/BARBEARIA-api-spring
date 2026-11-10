package br.edu.faculdade.barbearia;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public List<Cliente> buscar(String termo, Boolean ativo) {
        String t = (termo == null || termo.isBlank()) ? null : termo.trim();
        return repositorio.buscar(t, ativo);
    }

    @Transactional
    public boolean atualizar(String id, ClienteAtualizacao dados) {
        return repositorio.atualizar(id, dados);
    }

    public boolean desativar(String id) {
        return repositorio.desativar(id);
    }

    public boolean reativar(String id) {
        return repositorio.reativar(id);
    }
}