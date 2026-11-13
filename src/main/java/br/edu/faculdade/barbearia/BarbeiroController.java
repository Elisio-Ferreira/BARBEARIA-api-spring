package br.edu.faculdade.barbearia;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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

    @GetMapping("/busca")
    public List<Barbeiro> buscar(@RequestParam(required = false) String termo,
                                 @RequestParam(required = false) Boolean ativo) {
        return servico.buscar(termo, ativo);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Barbeiro> porId(@PathVariable UUID id) {
        return servico.buscarPorId(id.toString())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Barbeiro> atualizar(@PathVariable UUID id,
                                              @Valid @RequestBody BarbeiroAtualizacao dados) {
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

    // Mantido para não quebrar o que já existia (desativação lógica)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        return servico.desativar(id.toString())
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/servicos")
    public ResponseEntity<List<ServicoVinculado>> servicos(@PathVariable UUID id) {
        return servico.servicosDoBarbeiro(id.toString())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/servicos")
    public ResponseEntity<List<ServicoVinculado>> definirServicos(
            @PathVariable UUID id,
            @Valid @RequestBody BarbeiroServicosEntrada entrada) {
        return servico.definirServicos(id.toString(), entrada.servicoIds())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}