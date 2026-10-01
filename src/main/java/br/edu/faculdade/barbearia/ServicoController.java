package br.edu.faculdade.barbearia;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * Recebe o pedido e devolve a resposta. Cada método tem uma linha: nenhuma
 * regra mora aqui, só HTTP (status, cabeçalho, corpo).
 *
 * Nada de Servico aparece: entra ServicoEntrada, sai ServicoResposta.
 */
@RestController
@RequestMapping("/servicos")
public class ServicoController {

    private final ServicoService servico;

    public ServicoController(ServicoService servico) {
        this.servico = servico;
    }

    @GetMapping
    public List<ServicoResposta> listar() {
        return servico.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServicoResposta> porId(@PathVariable String id) {
        return servico.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ServicoResposta> criar(@Valid @RequestBody ServicoEntrada entrada) {
        ServicoResposta salvo = servico.criar(entrada);
        URI onde = URI.create("/servicos/" + salvo.id());
        return ResponseEntity.created(onde).body(salvo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServicoResposta> trocar(@PathVariable String id,
                                                  @Valid @RequestBody ServicoEntrada nova) {
        return servico.trocar(id, nova)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> apagar(@PathVariable String id) {
        return servico.apagar(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
