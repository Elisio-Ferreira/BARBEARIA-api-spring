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
                WHERE c.id = CAST(? AS UUID)
                """;

        return jdbcTemplate.query(sql, clienteMapper, id)
                .stream()
                .findFirst();
    }

    public List<Cliente> buscar(String termo, Boolean ativo) {

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
                WHERE (CAST(? AS TEXT) IS NULL
                       OR p.nome ILIKE '%' || CAST(? AS TEXT) || '%'
                       OR u.email ILIKE '%' || CAST(? AS TEXT) || '%'
                       OR p.telefone ILIKE '%' || CAST(? AS TEXT) || '%')
                  AND (CAST(? AS BOOLEAN) IS NULL
                       OR c.ativo = CAST(? AS BOOLEAN))
                ORDER BY p.nome
                """;

        return jdbcTemplate.query(sql, clienteMapper,
                termo, termo, termo, termo, ativo, ativo);
    }

    public boolean atualizar(String id, ClienteAtualizacao dados) {

        int perfis = jdbcTemplate.update("""
                UPDATE public.perfis
                SET nome = ?, telefone = ?
                WHERE id = (SELECT perfil_id
                            FROM public.clientes
                            WHERE id = CAST(? AS UUID))
                """, dados.nome(), dados.telefone(), id);

        if (perfis == 0) {
            return false;
        }

        jdbcTemplate.update("""
                UPDATE public.clientes
                SET endereco = ?
                WHERE id = CAST(? AS UUID)
                """, dados.endereco(), id);

        return true;
    }

    public boolean desativar(String id) {

        String sql = """
                UPDATE public.clientes
                SET ativo = false
                WHERE id = CAST(? AS UUID)
                  AND ativo = true
                """;

        return jdbcTemplate.update(sql, id) > 0;
    }

    public boolean reativar(String id) {

        String sql = """
                UPDATE public.clientes
                SET ativo = true
                WHERE id = CAST(? AS UUID)
                  AND ativo = false
                """;

        return jdbcTemplate.update(sql, id) > 0;
    }
}