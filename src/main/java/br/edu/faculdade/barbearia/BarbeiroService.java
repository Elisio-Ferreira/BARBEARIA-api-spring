package br.edu.faculdade.barbearia;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BarbeiroService {

    private final BarbeiroRepository repositorio;

    public BarbeiroService(BarbeiroRepository repositorio) {
        this.repositorio = repositorio;
    }

    public List<Barbeiro> listar() {
        return repositorio.todos();
    }

    public Optional<Barbeiro> buscarPorId(String id) {
        return repositorio.porId(id);
    }

    public List<Barbeiro> buscar(String termo, Boolean ativo) {
        String t = (termo == null || termo.isBlank()) ? null : termo.trim();
        return repositorio.buscar(t, ativo);
    }

    public Barbeiro criar(BarbeiroEntrada entrada, String perfilId) {

        Barbeiro novo = new Barbeiro(
                null,
                perfilId,
                entrada.nome(),
                entrada.telefone(),
                entrada.endereco(),
                entrada.email(),
                true
        );

        return repositorio.salvar(novo);
    }

    @Transactional
    public boolean atualizar(String id, BarbeiroAtualizacao dados) {
        return repositorio.atualizar(id, dados);
    }

    public boolean desativar(String id) {
        return repositorio.desativar(id);
    }

    public boolean reativar(String id) {
        return repositorio.reativar(id);
    }

    public Optional<List<ServicoVinculado>> servicosDoBarbeiro(String id) {
        if (!repositorio.existe(id)) {
            return Optional.empty();
        }
        return Optional.of(repositorio.servicosDoBarbeiro(id));
    }

    @Transactional
    public Optional<List<ServicoVinculado>> definirServicos(String id, List<UUID> servicoIds) {
        if (!repositorio.existe(id)) {
            return Optional.empty();
        }
        List<UUID> unicos = servicoIds.stream().distinct().toList();
        repositorio.definirServicos(id, unicos);
        return Optional.of(repositorio.servicosDoBarbeiro(id));
    }
}