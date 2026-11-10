package br.edu.faculdade.barbearia;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService servico;

    public AuthController(AuthService servico) {
        this.servico = servico;
    }

    @PostMapping("/login")
    public AuthResposta login(
            @Valid @RequestBody AuthEntrada entrada
    ) {
        return servico.entrar(entrada);
    }

    @PostMapping("/cadastro/cliente")
    public CadastroClienteResposta cadastrarCliente(
            @Valid @RequestBody CadastroClienteEntrada entrada
    ) {
        return servico.cadastrarCliente(entrada);
    }
}