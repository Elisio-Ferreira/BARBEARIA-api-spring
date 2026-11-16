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
public class AtendimentoController {

    private final AtendimentoService servico;

    public AtendimentoController(AtendimentoService servico) {
        this.servico = servico;
    }

    @PostMapping("/agendamentos/{agendamentoId}/atendimento")
    public ResponseEntity<Atendimento> registrar(
            @PathVariable UUID agendamentoId,
            @Valid @RequestBody(required = false) AtendimentoEntrada entrada) {

        String observacoes = entrada == null ? null : entrada.observacoes();
        Atendimento criado = servico.registrar(agendamentoId.toString(), observacoes);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @GetMapping("/agendamentos/{agendamentoId}/atendimento")
    public Atendimento porAgendamento(@PathVariable UUID agendamentoId) {
        return servico.porAgendamento(agendamentoId.toString());
    }

    @GetMapping("/atendimentos")
    public List<Atendimento> listar(
            @RequestParam(required = false) UUID clienteId,
            @RequestParam(required = false) UUID barbeiroId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) {

        return servico.listar(
                clienteId == null ? null : clienteId.toString(),
                barbeiroId == null ? null : barbeiroId.toString(),
                de, ate);
    }
}