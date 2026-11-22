package br.edu.faculdade.barbearia;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BarbeiroCadastroController {

    private final BarbeiroCadastroService servico;

    public BarbeiroCadastroController(BarbeiroCadastroService servico) {
        this.servico = servico;
    }

    @PostMapping("/barbeiros")
    public ResponseEntity<Barbeiro> criar(
            @Valid @RequestBody BarbeiroCadastroEntrada entrada) {
        Barbeiro criado = servico.criar(entrada);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }
}