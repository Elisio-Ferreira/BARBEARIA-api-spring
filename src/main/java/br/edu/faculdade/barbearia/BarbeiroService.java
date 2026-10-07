package br.edu.faculdade.barbearia;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * O mesmo formato do ServicoService, com a diferença da frente 2: barbeiro se
 * busca por slug, e o slug não pode se repetir.
 *
 * Não há "trocar" (PUT) de propósito: trocar o slug seria trocar o endereço do
 * recurso, e isso é uma decisão de contrato que fica para uma etapa futura.
 */
@Service
public class BarbeiroService {

    private final BarbeiroRepository repositorio;

    public BarbeiroService(BarbeiroRepository repositorio) {
        this.repositorio = repositorio;
    }

    public List<Barbeiro> listar() {
        return repositorio.todos();
    }

    public Optional<Barbeiro> buscarPorSlug(String slug) {
        return repositorio.porSlug(slug);
    }

    // A regra "slug único" é regra de negócio, então mora aqui, e não no controller.
    public Barbeiro criar(BarbeiroEntrada entrada) {
        if (repositorio.porSlug(entrada.slug()).isPresent()) {
            throw new BarbeiroJaExisteException(entrada.slug());
        }
        Barbeiro novo = new Barbeiro(UUID.randomUUID().toString(),
                entrada.nome(), entrada.slug(), entrada.especialidade(), entrada.descricao());
        return repositorio.salvar(novo);
    }

    public boolean apagar(String slug) {
        return repositorio.remover(slug);
    }
}