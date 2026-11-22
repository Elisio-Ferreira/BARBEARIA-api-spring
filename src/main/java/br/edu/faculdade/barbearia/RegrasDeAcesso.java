package br.edu.faculdade.barbearia;

import org.springframework.http.server.PathContainer;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.List;
import java.util.Set;

public final class RegrasDeAcesso {

    public enum Tipo { PUBLICO, AUTENTICADO, PAPEIS }

    public record Regra(String metodo, PathPattern padrao, Tipo tipo, Set<String> papeis) {

        boolean combina(String metodoRequisicao, PathContainer caminho) {
            boolean metodoOk = metodo == null || metodo.equalsIgnoreCase(metodoRequisicao);
            return metodoOk && padrao.matches(caminho);
        }
    }

    private static final String ADM = "administrador";
    private static final String BAR = "barbeiro";
    private static final String CLI = "cliente";

    private static final PathPatternParser PARSER = new PathPatternParser();

    // Qualquer rota que não esteja na lista abaixo fica só para o administrador
    private static final Regra PADRAO =
            papeis(null, "/**", ADM);

    private static final List<Regra> REGRAS = List.of(

            // ---------- Públicas ----------
            publico("POST", "/auth/login"),
            publico("POST", "/auth/cadastro/**"),
            publico("GET", "/servicos/**"),
            publico("GET", "/agendamentos/disponibilidade"),

            // ---------- Qualquer pessoa logada ----------
            autenticado("GET", "/auth/me"),

            // ---------- Só administrador ----------
            papeis(null, "/relatorios/**", ADM),
            papeis(null, "/estatisticas/**", ADM),
            papeis(null, "/servicos/**", ADM),

            // ---------- Agenda ----------
            papeis("GET", "/agenda", ADM, BAR),

            // ---------- Clientes ----------
            papeis("GET", "/clientes", ADM, BAR),
            papeis("GET", "/clientes/busca", ADM, BAR),
            papeis("GET", "/clientes/*", ADM, BAR, CLI),
            papeis("PUT", "/clientes/*", ADM, CLI),
            papeis(null, "/clientes/**", ADM),

            // ---------- Barbeiros ----------
            papeis("GET", "/barbeiros/*/bloqueios", ADM, BAR),
            autenticado("GET", "/barbeiros/**"),
            papeis(null, "/barbeiros/*/horarios", ADM, BAR),
            papeis(null, "/barbeiros/*/bloqueios/**", ADM, BAR),
            papeis(null, "/barbeiros/**", ADM),

            // ---------- Agendamentos ----------
            papeis("POST", "/agendamentos", ADM, BAR, CLI),
            autenticado("GET", "/agendamentos"),
            autenticado("GET", "/agendamentos/*"),
            papeis("PATCH", "/agendamentos/*/cancelar", ADM, BAR, CLI),
            papeis("PATCH", "/agendamentos/*/confirmar", ADM, BAR),
            papeis("PATCH", "/agendamentos/*/falta", ADM, BAR),
            papeis("PATCH", "/agendamentos/*/concluir", ADM, BAR),

            // ---------- Atendimentos ----------
            autenticado("GET", "/agendamentos/*/atendimento"),
            papeis("POST", "/agendamentos/*/atendimento", ADM, BAR),
            autenticado("GET", "/atendimentos"),

            // ---------- Pagamentos ----------
            autenticado("GET", "/agendamentos/*/pagamentos"),
            papeis("POST", "/agendamentos/*/pagamentos", ADM, BAR),
            papeis("PATCH", "/pagamentos/*/estornar", ADM)
    );

    private RegrasDeAcesso() {
    }

    public static Regra buscar(String metodo, String caminho) {
        PathContainer container = PathContainer.parsePath(caminho);

        for (Regra regra : REGRAS) {
            if (regra.combina(metodo, container)) {
                return regra;
            }
        }
        return PADRAO;
    }

    private static Regra publico(String metodo, String padrao) {
        return new Regra(metodo, PARSER.parse(padrao), Tipo.PUBLICO, Set.of());
    }

    private static Regra autenticado(String metodo, String padrao) {
        return new Regra(metodo, PARSER.parse(padrao), Tipo.AUTENTICADO, Set.of());
    }

    private static Regra papeis(String metodo, String padrao, String... papeis) {
        return new Regra(metodo, PARSER.parse(padrao), Tipo.PAPEIS, Set.of(papeis));
    }
}