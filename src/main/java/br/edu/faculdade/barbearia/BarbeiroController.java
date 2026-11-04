
package br.edu.faculdade.barbearia;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/barbeiros")
public class BarbeiroController {

    private final BarbeiroService servico;

    public BarbeiroController(BarbeiroService servico) {
        this.servico = servico;
    }

    @GetMapping
    public List<Barbeiro> listar() {
        return servico.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Barbeiro> porId(@PathVariable String id) {
        return servico.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativar(@PathVariable String id) {
        return servico.desativar(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
