package br.edu.faculdade.barbearia;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    private final RowMapper<ServicoVinculado> servicoMapper = (rs, rowNum) ->
            new ServicoVinculado(
                    rs.getString("id"),
                    rs.getString("nome")
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
                WHERE b.id = CAST(? AS UUID)
                """;

        return jdbcTemplate.query(sql, barbeiroMapper, id)
                .stream()
                .findFirst();
    }

    public List<Barbeiro> buscar(String termo, Boolean ativo) {

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
                WHERE (CAST(? AS TEXT) IS NULL
                       OR p.nome ILIKE '%' || CAST(? AS TEXT) || '%'
                       OR u.email ILIKE '%' || CAST(? AS TEXT) || '%'
                       OR p.telefone ILIKE '%' || CAST(? AS TEXT) || '%')
                  AND (CAST(? AS BOOLEAN) IS NULL
                       OR b.ativo = CAST(? AS BOOLEAN))
                ORDER BY p.nome
                """;

        return jdbcTemplate.query(sql, barbeiroMapper,
                termo, termo, termo, termo, ativo, ativo);
    }

    public Barbeiro salvar(Barbeiro barbeiro) {

        String sql = """
                INSERT INTO public.barbeiros (
                    perfil_id,
                    endereco,
                    ativo
                )
                VALUES (CAST(? AS UUID), ?, ?)
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

    public boolean atualizar(String id, BarbeiroAtualizacao dados) {

        int perfis = jdbcTemplate.update("""
                UPDATE public.perfis
                SET nome = ?, telefone = ?
                WHERE id = (SELECT perfil_id
                            FROM public.barbeiros
                            WHERE id = CAST(? AS UUID))
                """, dados.nome(), dados.telefone(), id);

        if (perfis == 0) {
            return false;
        }

        jdbcTemplate.update("""
                UPDATE public.barbeiros
                SET endereco = ?
                WHERE id = CAST(? AS UUID)
                """, dados.endereco(), id);

        return true;
    }

    public boolean desativar(String id) {

        String sql = """
                UPDATE public.barbeiros
                SET ativo = false
                WHERE id = CAST(? AS UUID)
                  AND ativo = true
                """;

        return jdbcTemplate.update(sql, id) > 0;
    }

    public boolean reativar(String id) {

        String sql = """
                UPDATE public.barbeiros
                SET ativo = true
                WHERE id = CAST(? AS UUID)
                  AND ativo = false
                """;

        return jdbcTemplate.update(sql, id) > 0;
    }

    public boolean existe(String id) {

        Integer total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM public.barbeiros WHERE id = CAST(? AS UUID)",
                Integer.class,
                id
        );

        return total != null && total > 0;
    }

    public List<ServicoVinculado> servicosDoBarbeiro(String barbeiroId) {

        String sql = """
                SELECT s.id, s.nome
                FROM public.barbeiro_servicos bs
                INNER JOIN public.servicos s
                    ON s.id = bs.servico_id
                WHERE bs.barbeiro_id = CAST(? AS UUID)
                ORDER BY s.nome
                """;

        return jdbcTemplate.query(sql, servicoMapper, barbeiroId);
    }

    public void definirServicos(String barbeiroId, List<UUID> servicoIds) {

        jdbcTemplate.update(
                "DELETE FROM public.barbeiro_servicos WHERE barbeiro_id = CAST(? AS UUID)",
                barbeiroId
        );

        for (UUID servicoId : servicoIds) {
            jdbcTemplate.update("""
                    INSERT INTO public.barbeiro_servicos (barbeiro_id, servico_id)
                    VALUES (CAST(? AS UUID), CAST(? AS UUID))
                    """, barbeiroId, servicoId.toString());
        }
    }
}