
package br.edu.faculdade.barbearia;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class BarbeiroRepository {

    private final JdbcTemplate jdbcTemplate;

    public BarbeiroRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Barbeiro> barbeiroMapper = (rs, rowNum) ->
            new Barbeiro(
                    rs.getString("id"),
                    rs.getString("perfil_id"),
                    rs.getString("nome"),
                    rs.getString("telefone"),
                    rs.getString("endereco"),
                    rs.getString("email"),
                    rs.getBoolean("ativo")
            );

    public List<Barbeiro> todos() {

        String sql = """
                SELECT
                    b.id,
                    b.perfil_id,
                    p.nome,
                    p.telefone,
                    b.endereco,
                    u.email,
                    b.ativo
                FROM public.barbeiros b
                INNER JOIN public.perfis p
                    ON p.id = b.perfil_id
                INNER JOIN auth.users u
                    ON u.id = p.id
                ORDER BY p.nome
                """;

        return jdbcTemplate.query(sql, barbeiroMapper);
    }

    public Optional<Barbeiro> porId(String id) {

        String sql = """
                SELECT
                    b.id,
                    b.perfil_id,
                    p.nome,
                    p.telefone,
                    b.endereco,
                    u.email,
                    b.ativo
                FROM public.barbeiros b
                INNER JOIN public.perfis p
                    ON p.id = b.perfil_id
                INNER JOIN auth.users u
                    ON u.id = p.id
                WHERE b.id = ?
                """;

        return jdbcTemplate.query(sql, barbeiroMapper, id)
                .stream()
                .findFirst();
    }

    public Barbeiro salvar(Barbeiro barbeiro) {

        String sql = """
                INSERT INTO public.barbeiros (
                    perfil_id,
                    endereco,
                    ativo
                )
                VALUES (?, ?, ?)
                RETURNING id
                """;

        String id = jdbcTemplate.queryForObject(
                sql,
                String.class,
                barbeiro.perfilId(),
                barbeiro.endereco(),
                barbeiro.ativo()
        );

        return porId(id)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Não foi possível recuperar o barbeiro cadastrado."
                        )
                );
    }

    public boolean desativar(String id) {

        String sql = """
                UPDATE public.barbeiros
                SET ativo = false
                WHERE id = ?
                  AND ativo = true
                """;

        return jdbcTemplate.update(sql, id) > 0;
    }
}
