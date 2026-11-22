package br.edu.faculdade.barbearia;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class AtendimentoRepository {

    private static final String SELECT_DETALHE = """
            SELECT t.id,
                   t.agendamento_id,
                   a.cliente_id,
                   pc.nome AS cliente_nome,
                   a.barbeiro_id,
                   pb.nome AS barbeiro_nome,
                   s.nome AS servico_nome,
                   a.preco_combinado,
                   t.data_realizacao,
                   t.observacoes
            FROM public.atendimentos t
            INNER JOIN public.agendamentos a ON a.id = t.agendamento_id
            INNER JOIN public.clientes c ON c.id = a.cliente_id
            INNER JOIN public.perfis pc ON pc.id = c.perfil_id
            INNER JOIN public.barbeiros b ON b.id = a.barbeiro_id
            INNER JOIN public.perfis pb ON pb.id = b.perfil_id
            INNER JOIN public.servicos s ON s.id = a.servico_id
            """;

    private final JdbcTemplate jdbcTemplate;

    public AtendimentoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static OffsetDateTime paraRecife(OffsetDateTime valor) {
        return valor.atZoneSameInstant(Fuso.RECIFE).toOffsetDateTime();
    }

    private final RowMapper<Atendimento> atendimentoMapper = (rs, rowNum) ->
            new Atendimento(
                    rs.getString("id"),
                    rs.getString("agendamento_id"),
                    rs.getString("cliente_id"),
                    rs.getString("cliente_nome"),
                    rs.getString("barbeiro_id"),
                    rs.getString("barbeiro_nome"),
                    rs.getString("servico_nome"),
                    rs.getBigDecimal("preco_combinado"),
                    paraRecife(rs.getObject("data_realizacao", OffsetDateTime.class)),
                    rs.getString("observacoes")
            );

    public boolean existePorAgendamento(String agendamentoId) {

        Integer total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM public.atendimentos "
                        + "WHERE agendamento_id = CAST(? AS UUID)",
                Integer.class,
                agendamentoId
        );

        return total != null && total > 0;
    }

    public String inserir(String agendamentoId,
                          OffsetDateTime dataRealizacao,
                          String observacoes) {

        String sql = """
                INSERT INTO public.atendimentos
                    (agendamento_id, data_realizacao, observacoes)
                VALUES (CAST(? AS UUID), ?, ?)
                RETURNING CAST(id AS TEXT)
                """;

        return jdbcTemplate.queryForObject(sql, String.class,
                agendamentoId, dataRealizacao, observacoes);
    }

    public Optional<Atendimento> porId(String id) {

        String sql = SELECT_DETALHE + """
                WHERE t.id = CAST(? AS UUID)
                """;

        return jdbcTemplate.query(sql, atendimentoMapper, id)
                .stream()
                .findFirst();
    }

    public Optional<Atendimento> porAgendamento(String agendamentoId) {

        String sql = SELECT_DETALHE + """
                WHERE t.agendamento_id = CAST(? AS UUID)
                """;

        return jdbcTemplate.query(sql, atendimentoMapper, agendamentoId)
                .stream()
                .findFirst();
    }

    public List<Atendimento> listar(String clienteId, String barbeiroId,
                                    OffsetDateTime de, OffsetDateTime ate) {

        String sql = SELECT_DETALHE + """
                WHERE (CAST(? AS TEXT) IS NULL OR a.cliente_id = CAST(? AS UUID))
                  AND (CAST(? AS TEXT) IS NULL OR a.barbeiro_id = CAST(? AS UUID))
                  AND (CAST(? AS TIMESTAMPTZ) IS NULL OR t.data_realizacao >= CAST(? AS TIMESTAMPTZ))
                  AND (CAST(? AS TIMESTAMPTZ) IS NULL OR t.data_realizacao < CAST(? AS TIMESTAMPTZ))
                ORDER BY t.data_realizacao DESC
                """;

        return jdbcTemplate.query(sql, atendimentoMapper,
                clienteId, clienteId,
                barbeiroId, barbeiroId,
                de, de,
                ate, ate);
    }
}