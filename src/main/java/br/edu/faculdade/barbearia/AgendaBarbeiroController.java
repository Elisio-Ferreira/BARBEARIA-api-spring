package br.edu.faculdade.barbearia;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/barbeiros/{barbeiroId}")
public class AgendaBarbeiroController {

    private final AgendaBarbeiroService servico;

    public AgendaBarbeiroController(AgendaBarbeiroService servico) {
        this.servico = servico;
    }

    @GetMapping("/horarios")
    public List<HorarioTrabalho> horarios(@PathVariable UUID barbeiroId) {
        return servico.listarHorarios(barbeiroId.toString());
    }

    @PutMapping("/horarios")
    public List<HorarioTrabalho> definirHorarios(
            @PathVariable UUID barbeiroId,
            @Valid @RequestBody HorariosTrabalhoEntrada entrada) {
        return servico.definirHorarios(barbeiroId.toString(), entrada.horarios());
    }

    @GetMapping("/bloqueios")
    public List<Bloqueio> bloqueios(@PathVariable UUID barbeiroId) {
        return servico.listarBloqueios(barbeiroId.toString());
    }

    @PostMapping("/bloqueios")
    public ResponseEntity<Bloqueio> criarBloqueio(
            @PathVariable UUID barbeiroId,
            @Valid @RequestBody BloqueioEntrada entrada) {
        Bloqueio criado = servico.criarBloqueio(barbeiroId.toString(), entrada);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @DeleteMapping("/bloqueios/{bloqueioId}")
    public ResponseEntity<Void> removerBloqueio(@PathVariable UUID barbeiroId,
                                                @PathVariable UUID bloqueioId) {
        servico.removerBloqueio(barbeiroId.toString(), bloqueioId.toString());
        return ResponseEntity.noContent().build();
    }
}