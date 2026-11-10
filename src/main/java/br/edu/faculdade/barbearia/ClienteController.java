package br.edu.faculdade.barbearia;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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

    @GetMapping("/busca")
    public List<Cliente> buscar(@RequestParam(required = false) String termo,
                                @RequestParam(required = false) Boolean ativo) {
        return servico.buscar(termo, ativo);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cliente> porId(@PathVariable UUID id) {
        return servico.buscarPorId(id.toString())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cliente> atualizar(@PathVariable UUID id,
                                             @Valid @RequestBody ClienteAtualizacao dados) {
        if (!servico.atualizar(id.toString(), dados)) {
            return ResponseEntity.notFound().build();
        }
        return servico.buscarPorId(id.toString())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/desativar")
    public ResponseEntity<Void> desativar(@PathVariable UUID id) {
        return servico.desativar(id.toString())
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/reativar")
    public ResponseEntity<Void> reativar(@PathVariable UUID id) {
        return servico.reativar(id.toString())
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}