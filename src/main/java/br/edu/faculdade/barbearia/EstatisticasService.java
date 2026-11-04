
package br.edu.faculdade.barbearia;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class EstatisticasService {

    private final ServicoRepository servicos;

    public EstatisticasService(ServicoRepository servicos) {
        this.servicos = servicos;
    }

    public Map<String, Object> resumo() {

        List<Servico> todos = servicos.todos();

        long total = todos.size();

        long ativos = todos.stream()
                .filter(Servico::ativo)
                .count();

        long inativos = total - ativos;

        BigDecimal precoMedio = todos.stream()
                .map(Servico::preco)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (total > 0) {
            precoMedio = precoMedio.divide(
                    BigDecimal.valueOf(total),
                    2,
                    RoundingMode.HALF_UP
            );
        }

        double duracaoMedia = todos.stream()
                .mapToInt(Servico::duracaoMinutos)
                .average()
                .orElse(0);

        Map<String, Object> resumo = new LinkedHashMap<>();

        resumo.put("totalDeServicos", total);
        resumo.put("servicosAtivos", ativos);
        resumo.put("servicosInativos", inativos);
        resumo.put("precoMedio", precoMedio);
        resumo.put("duracaoMediaEmMinutos", arredondar(duracaoMedia));

        return resumo;
    }

    private double arredondar(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
