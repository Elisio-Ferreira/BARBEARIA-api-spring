package br.edu.faculdade.barbearia;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BarbeiroPorServicoRepository {

    private final JdbcTemplate jdbcTemplate;

    public BarbeiroPorServicoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<BarbeiroResumo> mapper = (rs, rowNum) ->
            new BarbeiroResumo(
                    rs.getString("id"),
                    rs.getString("nome")
            );

    public List<BarbeiroResumo> doServico(String servicoId) {

        String sql = """
                SELECT CAST(b.id AS TEXT) AS id,
                       p.nome AS nome
                FROM public.barbeiro_servicos bs
                INNER JOIN public.barbeiros b ON b.id = bs.barbeiro_id
                INNER JOIN public.perfis p ON p.id = b.perfil_id
                WHERE bs.servico_id = CAST(? AS UUID)
                  AND b.ativo = true
                  AND p.ativo = true
                ORDER BY p.nome
                """;

        return jdbcTemplate.query(sql, mapper, servicoId);
    }
}