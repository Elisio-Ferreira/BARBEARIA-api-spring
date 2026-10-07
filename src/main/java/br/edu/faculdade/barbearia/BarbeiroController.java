package br.edu.faculdade.barbearia;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
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

    @GetMapping("/{slug}")
    public ResponseEntity<Barbeiro> porSlug(@PathVariable String slug) {
        return servico.buscarPorSlug(slug)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Barbeiro> criar(@Valid @RequestBody BarbeiroEntrada entrada) {
        Barbeiro salvo = servico.criar(entrada);
        // O Location aponta para o SLUG que o cliente escolheu, e não para o id
        // que a API inventou. Mesmo 201, com o endereço vindo de lados opostos
        // em relação à frente 1.
        URI onde = URI.create("/barbeiros/" + salvo.slug());
        return ResponseEntity.created(onde).body(salvo);
    }

    @DeleteMapping("/{slug}")
    public ResponseEntity<Void> apagar(@PathVariable String slug) {
        return servico.apagar(slug)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}