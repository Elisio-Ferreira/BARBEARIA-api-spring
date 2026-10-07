package br.edu.faculdade.barbearia;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService servico;

    public ClienteController(ClienteService servico) {
        this.servico = servico;
    }

    @GetMapping
    public List<Cliente> listar() {
        return servico.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cliente> porId(@PathVariable String id) {
        return servico.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Cliente> criar(@Valid @RequestBody ClienteEntrada entrada) {
        Cliente salvo = servico.criar(entrada);
        URI onde = URI.create("/clientes/" + salvo.id());
        return ResponseEntity.created(onde).body(salvo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cliente> trocar(@PathVariable String id,
                                          @Valid @RequestBody ClienteEntrada nova) {
        return servico.trocar(id, nova)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // O DELETE repetido devolve 204 e depois 404, e isso NÃO quebra a
    // idempotência: ela é sobre o estado do servidor, não sobre a resposta.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> apagar(@PathVariable String id) {
        return servico.apagar(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}