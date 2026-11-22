package br.edu.faculdade.barbearia;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class PagamentoController {

    private final PagamentoService servico;
    private final ControleDeAcesso acesso;

    public PagamentoController(PagamentoService servico, ControleDeAcesso acesso) {
        this.servico = servico;
        this.acesso = acesso;
    }

    @PostMapping("/agendamentos/{agendamentoId}/pagamentos")
    public ResponseEntity<Pagamento> registrar(
            @PathVariable UUID agendamentoId,
            @Valid @RequestBody PagamentoEntrada entrada,
            @RequestAttribute(AutenticacaoInterceptor.ATRIBUTO) UsuarioAutenticado usuario) {

        acesso.exigirAgendamento(usuario, agendamentoId.toString());

        Pagamento criado = servico.registrar(agendamentoId.toString(), entrada);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @GetMapping("/agendamentos/{agendamentoId}/pagamentos")
    public List<Pagamento> listar(
            @PathVariable UUID agendamentoId,
            @RequestAttribute(AutenticacaoInterceptor.ATRIBUTO) UsuarioAutenticado usuario) {

        acesso.exigirAgendamento(usuario, agendamentoId.toString());
        return servico.listar(agendamentoId.toString());
    }

    @PatchMapping("/pagamentos/{id}/estornar")
    public Pagamento estornar(@PathVariable UUID id) {
        return servico.estornar(id.toString());
    }
}