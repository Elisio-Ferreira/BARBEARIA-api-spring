
package br.edu.faculdade.barbearia;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ClienteRepository {

    private final JdbcTemplate jdbcTemplate;

    public ClienteRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Cliente> clienteMapper = (rs, rowNum) ->
            new Cliente(
                    rs.getString("id"),
                    rs.getString("perfil_id"),
                    rs.getString("nome"),
                    rs.getString("email"),
                    rs.getString("telefone"),
                    rs.getString("endereco"),
                    rs.getBoolean("ativo")
            );

    public List<Cliente> todos() {

        String sql = """
                SELECT
                    c.id,
                    c.perfil_id,
                    p.nome,
                    u.email,
                    p.telefone,
                    c.endereco,
                    c.ativo
                FROM public.clientes c
                INNER JOIN public.perfis p
                    ON p.id = c.perfil_id
                INNER JOIN auth.users u
                    ON u.id = p.id
                ORDER BY p.nome
                """;

        return jdbcTemplate.query(sql, clienteMapper);
    }

    public Optional<Cliente> porId(String id) {

        String sql = """
                SELECT
                    c.id,
                    c.perfil_id,
                    p.nome,
                    u.email,
                    p.telefone,
                    c.endereco,
                    c.ativo
                FROM public.clientes c
                INNER JOIN public.perfis p
                    ON p.id = c.perfil_id
                INNER JOIN auth.users u
                    ON u.id = p.id
                WHERE c.id = ?
                """;

        return jdbcTemplate.query(sql, clienteMapper, id)
                .stream()
                .findFirst();
    }

    public boolean desativar(String id) {

        String sql = """
                UPDATE public.clientes
                SET ativo = false
                WHERE id = ?
                  AND ativo = true
                """;

        return jdbcTemplate.update(sql, id) > 0;
    }
}
