
package br.edu.faculdade.barbearia;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    public boolean desativar(String id) {
        return repositorio.desativar(id);
    }
}
