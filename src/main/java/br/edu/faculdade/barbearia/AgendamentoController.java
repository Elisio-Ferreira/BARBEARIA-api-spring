package br.edu.faculdade.barbearia;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/agendamentos")
public class AgendamentoController {

    private final AgendamentoService servico;

    public AgendamentoController(AgendamentoService servico) {
        this.servico = servico;
    }

    @GetMapping("/disponibilidade")
    public List<HorarioDisponivel> disponibilidade(
            @RequestParam UUID barbeiroId,
            @RequestParam UUID servicoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return servico.disponibilidade(
                barbeiroId.toString(), servicoId.toString(), data);
    }

    @GetMapping
    public List<Agendamento> listar(
            @RequestParam(required = false) UUID clienteId,
            @RequestParam(required = false) UUID barbeiroId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
            @RequestParam(required = false) String status) {
        return servico.listar(
                clienteId == null ? null : clienteId.toString(),
                barbeiroId == null ? null : barbeiroId.toString(),
                de, ate, status);
    }

    @PostMapping
    public ResponseEntity<Agendamento> criar(
            @Valid @RequestBody AgendamentoEntrada entrada) {
        Agendamento criado = servico.criar(entrada);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Agendamento> porId(@PathVariable UUID id) {
        return servico.buscarPorId(id.toString())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/confirmar")
    public Agendamento confirmar(@PathVariable UUID id) {
        return servico.confirmar(id.toString());
    }

    @PatchMapping("/{id}/cancelar")
    public Agendamento cancelar(
            @PathVariable UUID id,
            @Valid @RequestBody(required = false) CancelamentoEntrada entrada) {
        String motivo = entrada == null ? null : entrada.motivo();
        return servico.cancelar(id.toString(), motivo);
    }

    @PatchMapping("/{id}/falta")
    public Agendamento falta(@PathVariable UUID id) {
        return servico.registrarFalta(id.toString());
    }

    @PatchMapping("/{id}/concluir")
    public Agendamento concluir(@PathVariable UUID id) {
        return servico.concluir(id.toString());
    }
}