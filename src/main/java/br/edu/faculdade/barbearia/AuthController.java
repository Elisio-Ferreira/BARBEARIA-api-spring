package br.edu.faculdade.barbearia;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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

    @PostMapping("/refresh")
    public AuthResposta renovar(
            @Valid @RequestBody RefreshEntrada entrada
    ) {
        return servico.renovar(entrada);
    }

    @PostMapping("/cadastro/cliente")
    public CadastroClienteResposta cadastrarCliente(
            @Valid @RequestBody CadastroClienteEntrada entrada
    ) {
        return servico.cadastrarCliente(entrada);
    }

    @GetMapping("/me")
    public UsuarioAutenticado me(
            @RequestAttribute(name = "usuarioAutenticado",
                    required = false) UsuarioAutenticado usuario
    ) {
        if (usuario == null) {
            throw new RegraDeNegocioException(HttpStatus.UNAUTHORIZED,
                    "Envie o token no cabeçalho Authorization: Bearer <token>.");
        }
        return usuario;
    }
}