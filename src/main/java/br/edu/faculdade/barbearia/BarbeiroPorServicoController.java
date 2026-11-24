package br.edu.faculdade.barbearia;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class BarbeiroPorServicoController {

    private final BarbeiroPorServicoService servico;

    public BarbeiroPorServicoController(BarbeiroPorServicoService servico) {
        this.servico = servico;
    }

    @GetMapping("/servicos/{servicoId}/barbeiros")
    public List<BarbeiroResumo> listar(@PathVariable UUID servicoId) {
        return servico.listar(servicoId.toString());
    }
}