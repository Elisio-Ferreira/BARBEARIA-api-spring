package br.edu.faculdade.barbearia;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class BarbeiroCadastroService {

    private final AuthService auth;
    private final BarbeiroCadastroRepository cadastro;
    private final BarbeiroRepository barbeiros;

    public BarbeiroCadastroService(AuthService auth,
                                   BarbeiroCadastroRepository cadastro,
                                   BarbeiroRepository barbeiros) {
        this.auth = auth;
        this.cadastro = cadastro;
        this.barbeiros = barbeiros;
    }

    public Barbeiro criar(BarbeiroCadastroEntrada entrada) {

        String endereco = (entrada.endereco() == null || entrada.endereco().isBlank())
                ? null : entrada.endereco().trim();

        // Erros do Supabase (e-mail já cadastrado, senha fraca...) são tratados pelo TratadorDeErros
        CadastroClienteResposta criado = auth.cadastrarCliente(
                new CadastroClienteEntrada(
                        entrada.nome().trim(),
                        entrada.telefone().trim(),
                        endereco,
                        entrada.email().trim(),
                        entrada.senha()));

        if (criado.id() == null || criado.id().isBlank()) {
            throw new RegraDeNegocioException(HttpStatus.BAD_GATEWAY,
                    "O serviço de autenticação não devolveu o usuário criado.");
        }

        String barbeiroId;
        try {
            barbeiroId = cadastro.converterClienteEmBarbeiro(criado.id(), endereco);
        } catch (RuntimeException ex) {
            ex.printStackTrace(); // detalhe fica só no console do servidor
            throw new RegraDeNegocioException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "O usuário foi criado, mas não foi possível concluir o cadastro "
                            + "como barbeiro. Verifique o cadastro de " + entrada.email() + ".");
        }

        return barbeiros.porId(barbeiroId)
                .orElseThrow(() -> new RegraDeNegocioException(
                        HttpStatus.NOT_FOUND, "Barbeiro não encontrado."));
    }
}