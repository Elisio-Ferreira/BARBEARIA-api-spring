package br.edu.faculdade.barbearia;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class RemarcacaoController {

    private final RemarcacaoService servico;
    private final ControleDeAcesso acesso;

    public RemarcacaoController(RemarcacaoService servico, ControleDeAcesso acesso) {
        this.servico = servico;
        this.acesso = acesso;
    }

    @PatchMapping("/agendamentos/{id}/remarcar")
    public Agendamento remarcar(
            @PathVariable UUID id,
            @Valid @RequestBody RemarcacaoEntrada entrada,
            @RequestAttribute(AutenticacaoInterceptor.ATRIBUTO) UsuarioAutenticado usuario) {

        acesso.exigirAgendamento(usuario, id.toString());
        return servico.remarcar(id.toString(), entrada.inicio());
    }
}