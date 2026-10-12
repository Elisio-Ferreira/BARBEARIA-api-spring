package br.edu.faculdade.barbearia;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class EstatisticasService {

    private final ServicoRepository servicos;

    public EstatisticasService(ServicoRepository servicos) {
        this.servicos = servicos;
    }

    public Map<String, Object> resumo() {
        List<Servico> todos = servicos.todos();

        Map<String, Object> resumo = new LinkedHashMap<>();
        resumo.put("totalDeServicos", todos.size());
        resumo.put("emPromocao", todos.stream().filter(Servico::promocao).count());
        resumo.put("precoMedio", arredondar(
                todos.stream().mapToDouble(Servico::preco).average().orElse(0)));
        resumo.put("duracaoMediaEmMinutos", arredondar(
                todos.stream().mapToInt(Servico::duracaoMinutos).average().orElse(0)));
        resumo.put("porCategoria", todos.stream()
                .collect(Collectors.groupingBy(Servico::categoria, TreeMap::new, Collectors.counting())));
        resumo.put("porBarbeiro", todos.stream()
                .collect(Collectors.groupingBy(Servico::barbeiroSlug, TreeMap::new, Collectors.counting())));
        return resumo;
    }

    private double arredondar(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}