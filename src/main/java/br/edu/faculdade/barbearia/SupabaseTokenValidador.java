package br.edu.faculdade.barbearia;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SupabaseTokenValidador {

    private static final ParameterizedTypeReference<Map<String, Object>> MAPA =
            new ParameterizedTypeReference<>() {
            };

    private static final Duration VALIDADE_CACHE = Duration.ofSeconds(60);
    private static final int LIMITE_CACHE = 1000;

    private record Entrada(String perfilId, Instant expira) {
    }

    private final Map<String, Entrada> cache = new ConcurrentHashMap<>();
    private final RestClient supabase;
    private final String anonKey;

    public SupabaseTokenValidador(
            @Value("${supabase.url}") String supabaseUrl,
            @Value("${supabase.anon-key}") String anonKey
    ) {
        this.supabase = RestClient.builder()
                .baseUrl(supabaseUrl)
                .build();
        this.anonKey = anonKey;
    }

    /** Devolve o id do perfil dono do token, ou vazio se o token for inválido. */
    public Optional<String> validar(String token) {

        Entrada guardada = cache.get(token);
        if (guardada != null && guardada.expira().isAfter(Instant.now())) {
            return Optional.of(guardada.perfilId());
        }

        try {
            Map<String, Object> resposta = supabase.get()
                    .uri("/auth/v1/user")
                    .header("apikey", anonKey)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .body(MAPA);

            Object id = resposta == null ? null : resposta.get("id");
            if (id == null) {
                return Optional.empty();
            }

            if (cache.size() >= LIMITE_CACHE) {
                cache.clear();
            }
            cache.put(token, new Entrada(id.toString(),
                    Instant.now().plus(VALIDADE_CACHE)));

            return Optional.of(id.toString());

        } catch (HttpClientErrorException ex) {
            int status = ex.getStatusCode().value();
            if (status == 400 || status == 401 || status == 403) {
                cache.remove(token);
                return Optional.empty();
            }
            throw ex;
        }
    }
}