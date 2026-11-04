
package br.edu.faculdade.barbearia;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ServicoService {

    private final ServicoRepository repositorio;

    public ServicoService(ServicoRepository repositorio) {
        this.repositorio = repositorio;
    }

    public List<ServicoResposta> listar() {
        return repositorio.todos()
                .stream()
                .map(ServicoResposta::de)
                .toList();
    }

    public Optional<ServicoResposta> buscarPorId(String id) {
        return repositorio.porId(id)
                .map(ServicoResposta::de);
    }

    public ServicoResposta criar(ServicoEntrada entrada) {

        Servico novo = new Servico(
                null,
                entrada.nome(),
                entrada.descricao(),
                entrada.preco(),
                entrada.duracaoMinutos(),
                true
        );

        return ServicoResposta.de(repositorio.salvar(novo));
    }

    public Optional<ServicoResposta> trocar(
            String id,
            ServicoEntrada entrada
    ) {

        Optional<Servico> existente = repositorio.porId(id);

        if (existente.isEmpty()) {
            return Optional.empty();
        }

        Servico atualizado = new Servico(
                id,
                entrada.nome(),
                entrada.descricao(),
                entrada.preco(),
                entrada.duracaoMinutos(),
                existente.get().ativo()
        );

        return repositorio.trocar(id, atualizado)
                .map(ServicoResposta::de);
    }

    public boolean desativar(String id) {
        return repositorio.desativar(id);
    }
}
