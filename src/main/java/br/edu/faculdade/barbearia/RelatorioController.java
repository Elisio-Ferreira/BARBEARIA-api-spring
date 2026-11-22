package br.edu.faculdade.barbearia;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
public class RelatorioController {

    private final RelatorioService servico;
    private final ControleDeAcesso acesso;

    public RelatorioController(RelatorioService servico, ControleDeAcesso acesso) {
        this.servico = servico;
        this.acesso = acesso;
    }

    @GetMapping("/agenda")
    public List<AgendaItem> agenda(
            @RequestParam(required = false) UUID barbeiroId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
            @RequestParam(required = false) String status,
            @RequestAttribute(AutenticacaoInterceptor.ATRIBUTO) UsuarioAutenticado usuario) {

        String barbeiro = acesso.filtroBarbeiro(usuario,
                barbeiroId == null ? null : barbeiroId.toString());

        return servico.agenda(barbeiro, de, ate, status);
    }

    @GetMapping("/relatorios/atendimentos")
    public RelatorioAtendimentos atendimentos(
            @RequestParam(required = false) UUID barbeiroId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) {

        return servico.atendimentos(
                barbeiroId == null ? null : barbeiroId.toString(), de, ate);
    }

    @GetMapping("/relatorios/faturamento")
    public RelatorioFaturamento faturamento(
            @RequestParam(required = false) UUID barbeiroId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) {

        return servico.faturamento(
                barbeiroId == null ? null : barbeiroId.toString(), de, ate);
    }
}