
package br.edu.faculdade.barbearia;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BuscaService {

    private final ServicoRepository repositorio;

    public BuscaService(ServicoRepository repositorio) {
        this.repositorio = repositorio;
    }

    public List<ServicoResposta> buscar(BigDecimal precoMax) {

        return repositorio.todos()
                .stream()

                .filter(Servico::ativo)

                .filter(s ->
                        precoMax == null ||
                                s.preco().compareTo(precoMax) <= 0
                )

                .map(ServicoResposta::de)
                .toList();
    }
}
