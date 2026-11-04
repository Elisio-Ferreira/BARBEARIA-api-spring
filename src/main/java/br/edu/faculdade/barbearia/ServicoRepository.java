
package br.edu.faculdade.barbearia;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ServicoRepository {

    private final JdbcTemplate jdbcTemplate;

    public ServicoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Servico> servicoMapper = (rs, rowNum) ->
            new Servico(
                    rs.getString("id"),
                    rs.getString("nome"),
                    rs.getString("descricao"),
                    rs.getBigDecimal("preco"),
                    rs.getInt("duracao_minutos"),
                    rs.getBoolean("ativo")
            );

    public List<Servico> todos() {

        String sql = """
                SELECT
                    id,
                    nome,
                    descricao,
                    preco,
                    duracao_minutos,
                    ativo
                FROM public.servicos
                ORDER BY nome
                """;

        return jdbcTemplate.query(sql, servicoMapper);
    }

    public Optional<Servico> porId(String id) {

        String sql = """
                SELECT
                    id,
                    nome,
                    descricao,
                    preco,
                    duracao_minutos,
                    ativo
                FROM public.servicos
                WHERE id = ?
                """;

        return jdbcTemplate.query(sql, servicoMapper, id)
                .stream()
                .findFirst();
    }

    public Servico salvar(Servico novo) {

        String sql = """
                INSERT INTO public.servicos (
                    nome,
                    descricao,
                    preco,
                    duracao_minutos,
                    ativo
                )
                VALUES (?, ?, ?, ?, ?)
                RETURNING id
                """;

        String id = jdbcTemplate.queryForObject(
                sql,
                String.class,
                novo.nome(),
                novo.descricao(),
                novo.preco(),
                novo.duracaoMinutos(),
                novo.ativo()
        );

        return porId(id)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Não foi possível recuperar o serviço cadastrado."
                        )
                );
    }

    public Optional<Servico> trocar(String id, Servico novo) {

        String sql = """
                UPDATE public.servicos
                SET
                    nome = ?,
                    descricao = ?,
                    preco = ?,
                    duracao_minutos = ?,
                    ativo = ?
                WHERE id = ?
                """;

        int linhasAlteradas = jdbcTemplate.update(
                sql,
                novo.nome(),
                novo.descricao(),
                novo.preco(),
                novo.duracaoMinutos(),
                novo.ativo(),
                id
        );

        if (linhasAlteradas == 0) {
            return Optional.empty();
        }

        return porId(id);
    }

    public boolean desativar(String id) {

        String sql = """
                UPDATE public.servicos
                SET ativo = false
                WHERE id = ?
                  AND ativo = true
                """;

        return jdbcTemplate.update(sql, id) > 0;
    }
}
