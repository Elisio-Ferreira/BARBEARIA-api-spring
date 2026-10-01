package br.edu.faculdade.barbearia;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Onde a decisão mora, e onde a tradução mora também:
 * ServicoEntrada vira Servico na hora de gravar, e Servico vira
 * ServicoResposta na hora de responder. O controller só fala HTTP.
 */
@Service
public class ServicoService {

    private final ServicoRepository repositorio;

    public ServicoService(ServicoRepository repositorio) {
        this.repositorio = repositorio;
    }

    public List<ServicoResposta> listar() {
        return repositorio.todos().stream()
                .map(ServicoResposta::de)
                .toList();
    }

    public Optional<ServicoResposta> buscarPorId(String id) {
        return repositorio.porId(id).map(ServicoResposta::de);
    }

    public ServicoResposta criar(ServicoEntrada entrada) {
        Servico novo = paraServico(UUID.randomUUID().toString(), entrada);
        return ServicoResposta.de(repositorio.salvar(novo));
    }

    // O id vem da URL, não do corpo: trocar é sempre "o serviço {id} passa a ser isto".
    public Optional<ServicoResposta> trocar(String id, ServicoEntrada nova) {
        return repositorio.trocar(id, paraServico(id, nova)).map(ServicoResposta::de);
    }

    public boolean apagar(String id) {
        return repositorio.remover(id);
    }

    // Boolean.TRUE.equals: null ("não informou") vira false.
    private Servico paraServico(String id, ServicoEntrada e) {
        return new Servico(id, e.nome(), e.descricao(), e.categoria(),
                e.preco(), e.duracaoMinutos(),
                Boolean.TRUE.equals(e.promocao()), e.barbeiroSlug());
    }
}
