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
    private final ControleDeAcesso acesso;

    public AtendimentoController(AtendimentoService servico, ControleDeAcesso acesso) {
        this.servico = servico;
        this.acesso = acesso;
    }

    @PostMapping("/agendamentos/{agendamentoId}/atendimento")
    public ResponseEntity<Atendimento> registrar(
            @PathVariable UUID agendamentoId,
            @Valid @RequestBody(required = false) AtendimentoEntrada entrada,
            @RequestAttribute(AutenticacaoInterceptor.ATRIBUTO) UsuarioAutenticado usuario) {

        acesso.exigirAgendamento(usuario, agendamentoId.toString());

        String observacoes = entrada == null ? null : entrada.observacoes();
        Atendimento criado = servico.registrar(agendamentoId.toString(), observacoes);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @GetMapping("/agendamentos/{agendamentoId}/atendimento")
    public Atendimento porAgendamento(
            @PathVariable UUID agendamentoId,
            @RequestAttribute(AutenticacaoInterceptor.ATRIBUTO) UsuarioAutenticado usuario) {

        acesso.exigirAgendamento(usuario, agendamentoId.toString());
        return servico.porAgendamento(agendamentoId.toString());
    }

    @GetMapping("/atendimentos")
    public List<Atendimento> listar(
            @RequestParam(required = false) UUID clienteId,
            @RequestParam(required = false) UUID barbeiroId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
            @RequestAttribute(AutenticacaoInterceptor.ATRIBUTO) UsuarioAutenticado usuario) {

        String cliente = acesso.filtroCliente(usuario,
                clienteId == null ? null : clienteId.toString());
        String barbeiro = acesso.filtroBarbeiro(usuario,
                barbeiroId == null ? null : barbeiroId.toString());

        return servico.listar(cliente, barbeiro, de, ate);
    }
}