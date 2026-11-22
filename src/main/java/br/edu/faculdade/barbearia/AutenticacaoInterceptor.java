package br.edu.faculdade.barbearia;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AutenticacaoInterceptor implements HandlerInterceptor {

    public static final String ATRIBUTO = "usuarioAutenticado";

    private final SupabaseTokenValidador validador;
    private final AutenticacaoRepository repositorio;

    public AutenticacaoInterceptor(SupabaseTokenValidador validador,
                                   AutenticacaoRepository repositorio) {
        this.validador = validador;
        this.repositorio = repositorio;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {

        // Pré-verificação do navegador (CORS) não leva token
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        RegrasDeAcesso.Regra regra = RegrasDeAcesso.buscar(
                request.getMethod(), request.getRequestURI());

        if (regra.tipo() == RegrasDeAcesso.Tipo.PUBLICO) {
            return true;
        }

        String cabecalho = request.getHeader("Authorization");

        if (cabecalho == null || cabecalho.isBlank()) {
            throw new RegraDeNegocioException(HttpStatus.UNAUTHORIZED,
                    "Envie o token no cabeçalho Authorization: Bearer <token>.");
        }

        if (!cabecalho.regionMatches(true, 0, "Bearer ", 0, 7)) {
            throw new RegraDeNegocioException(HttpStatus.UNAUTHORIZED,
                    "Use o cabeçalho no formato: Authorization: Bearer <token>.");
        }

        String token = cabecalho.substring(7).trim();

        String perfilId = validador.validar(token)
                .orElseThrow(() -> new RegraDeNegocioException(
                        HttpStatus.UNAUTHORIZED, "Token inválido ou expirado."));

        UsuarioAutenticado usuario = repositorio.buscarPorPerfil(perfilId)
                .orElseThrow(() -> new RegraDeNegocioException(
                        HttpStatus.FORBIDDEN,
                        "Este usuário não possui perfil no sistema."));

        if (!usuario.ativo()) {
            throw new RegraDeNegocioException(HttpStatus.FORBIDDEN,
                    "Esta conta está inativa.");
        }

        if (regra.tipo() == RegrasDeAcesso.Tipo.PAPEIS
                && !regra.papeis().contains(usuario.papel())) {
            throw new RegraDeNegocioException(HttpStatus.FORBIDDEN,
                    "Você não tem permissão para esta operação.");
        }

        request.setAttribute(ATRIBUTO, usuario);
        return true;
    }
}