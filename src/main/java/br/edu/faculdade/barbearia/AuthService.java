package br.edu.faculdade.barbearia;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class AuthService {

    private static final ParameterizedTypeReference<Map<String, Object>> MAPA =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient supabase;
    private final String anonKey;

    public AuthService(
            @Value("${supabase.url}") String supabaseUrl,
            @Value("${supabase.anon-key}") String anonKey
    ) {
        this.supabase = RestClient.builder()
                .baseUrl(supabaseUrl)
                .build();

        this.anonKey = anonKey;
    }

    public AuthResposta entrar(AuthEntrada entrada) {

        Map<String, Object> resposta = supabase.post()
                .uri("/auth/v1/token?grant_type=password")
                .header("apikey", anonKey)
                .body(Map.of(
                        "email", entrada.email(),
                        "password", entrada.senha()
                ))
                .retrieve()
                .body(MAPA);

        if (resposta == null) {
            throw new IllegalStateException(
                    "O Supabase não retornou os dados do login."
            );
        }

        Map<String, Object> usuario = paraMapa(resposta.get("user"));

        Object expira = resposta.get("expires_in");
        long expiresIn = expira instanceof Number n ? n.longValue() : 0L;

        return new AuthResposta(
                texto(resposta, "access_token"),
                texto(resposta, "refresh_token"),
                texto(resposta, "token_type"),
                expiresIn,
                texto(usuario, "id"),
                texto(usuario, "email")
        );
    }

    public CadastroClienteResposta cadastrarCliente(CadastroClienteEntrada entrada) {

        Map<String, Object> resposta = supabase.post()
                .uri("/auth/v1/signup")
                .header("apikey", anonKey)
                .body(Map.of(
                        "email", entrada.email(),
                        "password", entrada.senha(),
                        "data", Map.of(
                                "nome", entrada.nome(),
                                "telefone", entrada.telefone(),
                                "endereco", entrada.endereco() == null
                                        ? "" : entrada.endereco()
                        )
                ))
                .retrieve()
                .body(MAPA);

        if (resposta == null) {
            throw new IllegalStateException(
                    "O Supabase não retornou os dados do cadastro."
            );
        }

        // Com confirmação de e-mail ligada, o usuário vem na raiz.
        // Sem confirmação, vem dentro de "user".
        Map<String, Object> usuario = resposta.containsKey("user")
                ? paraMapa(resposta.get("user"))
                : resposta;

        return new CadastroClienteResposta(
                texto(usuario, "id"),
                texto(usuario, "email")
        );
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> paraMapa(Object valor) {
        return valor instanceof Map<?, ?> m
                ? (Map<String, Object>) m
                : Map.of();
    }

    private String texto(Map<String, Object> mapa, String chave) {
        Object valor = mapa.get(chave);
        return valor == null ? "" : valor.toString();
    }
}