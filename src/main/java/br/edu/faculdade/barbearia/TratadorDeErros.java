package br.edu.faculdade.barbearia;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class TratadorDeErros {

    @ExceptionHandler(RegraDeNegocioException.class)
    public ProblemDetail regraDeNegocio(RegraDeNegocioException ex) {
        return problema(ex.getStatus(), titulo(ex.getStatus()), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validacao(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> campos.putIfAbsent(e.getField(), e.getDefaultMessage()));

        ProblemDetail p = problema(HttpStatus.BAD_REQUEST, "Dados inválidos",
                "Um ou mais campos estão inválidos.");
        p.setProperty("campos", campos);
        return p;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail tipoInvalido(MethodArgumentTypeMismatchException ex) {
        return problema(HttpStatus.BAD_REQUEST, "Parâmetro inválido",
                "O valor informado para '" + ex.getName() + "' não é válido.");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail jsonInvalido(HttpMessageNotReadableException ex) {
        return problema(HttpStatus.BAD_REQUEST, "Corpo inválido",
                "O corpo da requisição está ausente ou não é um JSON válido.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail integridade(DataIntegrityViolationException ex) {
        return problema(HttpStatus.CONFLICT, "Conflito de dados",
                "A operação viola uma regra do banco (dado duplicado, "
                        + "horário já ocupado ou registro relacionado).");
    }

    // Erros devolvidos pelo Supabase Auth (login, cadastro)
    @ExceptionHandler(HttpClientErrorException.class)
    public ProblemDetail supabaseCliente(HttpClientErrorException ex) {
        String corpo = ex.getResponseBodyAsString();
        int status = ex.getStatusCode().value();

        if (corpo.contains("over_email_send_rate_limit") || status == 429) {
            return problema(HttpStatus.TOO_MANY_REQUESTS, "Limite atingido",
                    "Muitas tentativas de envio de e-mail. Tente novamente mais tarde.");
        }
        if (corpo.contains("user_already_exists")
                || corpo.contains("email_exists")
                || corpo.contains("already registered")) {
            return problema(HttpStatus.CONFLICT, "E-mail já cadastrado",
                    "Já existe um usuário com este e-mail.");
        }
        if (corpo.contains("weak_password")) {
            return problema(HttpStatus.BAD_REQUEST, "Senha fraca",
                    "A senha não atende aos requisitos de segurança.");
        }
        if (corpo.contains("invalid_credentials")
                || corpo.contains("Invalid login credentials")) {
            return problema(HttpStatus.UNAUTHORIZED, "Credenciais inválidas",
                    "E-mail ou senha incorretos.");
        }
        if (corpo.contains("email_not_confirmed")) {
            return problema(HttpStatus.FORBIDDEN, "E-mail não confirmado",
                    "Confirme seu e-mail antes de entrar.");
        }
        if (status == 400 || status == 401 || status == 403 || status == 422) {
            return problema(HttpStatus.valueOf(status == 422 ? 400 : status),
                    "Falha na autenticação",
                    "O serviço de autenticação recusou a requisição.");
        }
        return problema(HttpStatus.BAD_GATEWAY, "Erro no serviço de autenticação",
                "Não foi possível concluir a operação.");
    }

    @ExceptionHandler({HttpServerErrorException.class, ResourceAccessException.class})
    public ProblemDetail supabaseIndisponivel(Exception ex) {
        return problema(HttpStatus.BAD_GATEWAY, "Serviço indisponível",
                "O serviço de autenticação não respondeu. Tente novamente.");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail generico(Exception ex) {
        // Erros do próprio Spring (rota inexistente, método não permitido, etc.)
        if (ex instanceof ErrorResponse resposta) {
            return resposta.getBody();
        }

        ex.printStackTrace(); // fica só no console do servidor
        return problema(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno",
                "Ocorreu um erro inesperado.");
    }

    private String titulo(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> "Requisição inválida";
            case UNAUTHORIZED -> "Não autenticado";
            case FORBIDDEN -> "Acesso negado";
            case NOT_FOUND -> "Não encontrado";
            case CONFLICT -> "Conflito";
            default -> "Erro";
        };
    }

    private ProblemDetail problema(HttpStatusCode status, String titulo, String detalhe) {
        ProblemDetail p = ProblemDetail.forStatusAndDetail(status, detalhe);
        p.setTitle(titulo);
        p.setType(URI.create("about:blank"));
        return p;
    }
}