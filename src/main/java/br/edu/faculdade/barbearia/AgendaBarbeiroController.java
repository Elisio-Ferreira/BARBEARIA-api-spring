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
    private final ControleDeAcesso acesso;

    public AgendaBarbeiroController(AgendaBarbeiroService servico,
                                    ControleDeAcesso acesso) {
        this.servico = servico;
        this.acesso = acesso;
    }

    @GetMapping("/horarios")
    public List<HorarioTrabalho> horarios(
            @PathVariable UUID barbeiroId,
            @RequestAttribute(AutenticacaoInterceptor.ATRIBUTO) UsuarioAutenticado usuario) {

        acesso.exigirBarbeiro(usuario, barbeiroId.toString());
        return servico.listarHorarios(barbeiroId.toString());
    }

    @PutMapping("/horarios")
    public List<HorarioTrabalho> definirHorarios(
            @PathVariable UUID barbeiroId,
            @Valid @RequestBody HorariosTrabalhoEntrada entrada,
            @RequestAttribute(AutenticacaoInterceptor.ATRIBUTO) UsuarioAutenticado usuario) {

        acesso.exigirBarbeiro(usuario, barbeiroId.toString());
        return servico.definirHorarios(barbeiroId.toString(), entrada.horarios());
    }

    @GetMapping("/bloqueios")
    public List<Bloqueio> bloqueios(
            @PathVariable UUID barbeiroId,
            @RequestAttribute(AutenticacaoInterceptor.ATRIBUTO) UsuarioAutenticado usuario) {

        acesso.exigirBarbeiro(usuario, barbeiroId.toString());
        return servico.listarBloqueios(barbeiroId.toString());
    }

    @PostMapping("/bloqueios")
    public ResponseEntity<Bloqueio> criarBloqueio(
            @PathVariable UUID barbeiroId,
            @Valid @RequestBody BloqueioEntrada entrada,
            @RequestAttribute(AutenticacaoInterceptor.ATRIBUTO) UsuarioAutenticado usuario) {

        acesso.exigirBarbeiro(usuario, barbeiroId.toString());
        Bloqueio criado = servico.criarBloqueio(barbeiroId.toString(), entrada);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @DeleteMapping("/bloqueios/{bloqueioId}")
    public ResponseEntity<Void> removerBloqueio(
            @PathVariable UUID barbeiroId,
            @PathVariable UUID bloqueioId,
            @RequestAttribute(AutenticacaoInterceptor.ATRIBUTO) UsuarioAutenticado usuario) {

        acesso.exigirBarbeiro(usuario, barbeiroId.toString());
        servico.removerBloqueio(barbeiroId.toString(), bloqueioId.toString());
        return ResponseEntity.noContent().build();
    }
}