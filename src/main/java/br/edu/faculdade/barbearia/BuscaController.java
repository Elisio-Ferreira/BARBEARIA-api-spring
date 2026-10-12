package br.edu.faculdade.barbearia;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/servicos")
public class BuscaController {

    private final BuscaService servico;

    public BuscaController(BuscaService servico) {
        this.servico = servico;
    }

    @GetMapping("/busca")
    public List<ServicoResposta> buscar(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String barbeiro,
            @RequestParam(required = false) Double precoMax,
            @RequestParam(required = false) Boolean promocao) {
        return servico.buscar(categoria, barbeiro, precoMax, promocao);
    }
}