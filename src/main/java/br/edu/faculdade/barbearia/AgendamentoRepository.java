package br.edu.faculdade.barbearia;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class AgendamentoRepository {

    private static final String COLUNAS = """
            id, cliente_id, barbeiro_id, servico_id, inicio, fim,
            preco_combinado, duracao_minutos, status, motivo_cancelamento
            """;

    private final JdbcTemplate jdbcTemplate;

    public AgendamentoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static OffsetDateTime paraRecife(OffsetDateTime valor) {
        return valor.atZoneSameInstant(Fuso.RECIFE).toOffsetDateTime();
    }

    private final RowMapper<Agendamento> agendamentoMapper = (rs, rowNum) ->
            new Agendamento(
                    rs.getString("id"),
                    rs.getString("cliente_id"),
                    rs.getString("barbeiro_id"),
                    rs.getString("servico_id"),
                    paraRecife(rs.getObject("inicio", OffsetDateTime.class)),
                    paraRecife(rs.getObject("fim", OffsetDateTime.class)),
                    rs.getBigDecimal("preco_combinado"),
                    rs.getInt("duracao_minutos"),
                    rs.getString("status"),
                    rs.getString("motivo_cancelamento")
            );

    private final RowMapper<ServicoAgendavel> servicoMapper = (rs, rowNum) ->
            new ServicoAgendavel(
                    rs.getString("id"),
                    rs.getString("nome"),
                    rs.getBigDecimal("preco"),
                    rs.getInt("duracao_minutos"),
                    rs.getBoolean("ativo")
            );

    public Optional<ServicoAgendavel> servico(String servicoId) {

        String sql = """
                SELECT id, nome, preco, duracao_minutos, ativo
                FROM public.servicos
                WHERE id = CAST(? AS UUID)
                """;

        return jdbcTemplate.query(sql, servicoMapper, servicoId)
                .stream()
                .findFirst();
    }

    public boolean servicoVinculado(String barbeiroId, String servicoId) {

        String sql = """
                SELECT COUNT(*)
                FROM public.barbeiro_servicos
                WHERE barbeiro_id = CAST(? AS UUID)
                  AND servico_id = CAST(? AS UUID)
                """;

        Integer total = jdbcTemplate.queryForObject(
                sql, Integer.class, barbeiroId, servicoId);

        return total != null && total > 0;
    }

    public List<Agendamento> ocupados(String barbeiroId,
                                      OffsetDateTime inicio,
                                      OffsetDateTime fim) {

        String sql = "SELECT " + COLUNAS + """
                FROM public.agendamentos
                WHERE barbeiro_id = CAST(? AS UUID)
                  AND status IN ('agendado', 'confirmado')
                  AND inicio < CAST(? AS TIMESTAMPTZ)
                  AND fim > CAST(? AS TIMESTAMPTZ)
                ORDER BY inicio
                """;

        return jdbcTemplate.query(sql, agendamentoMapper,
                barbeiroId, fim, inicio);
    }

    public Agendamento inserir(String clienteId, String barbeiroId,
                               String servicoId, OffsetDateTime inicio,
                               OffsetDateTime fim, BigDecimal preco,
                               int duracaoMinutos) {

        String sql = """
                INSERT INTO public.agendamentos
                    (cliente_id, barbeiro_id, servico_id, inicio, fim,
                     preco_combinado, duracao_minutos, status)
                VALUES (CAST(? AS UUID), CAST(? AS UUID), CAST(? AS UUID),
                        ?, ?, ?, ?, 'agendado')
                RETURNING """ + " " + COLUNAS;

        return jdbcTemplate.queryForObject(sql, agendamentoMapper,
                clienteId, barbeiroId, servicoId,
                inicio, fim, preco, duracaoMinutos);
    }

    public Optional<Agendamento> porId(String id) {

        String sql = "SELECT " + COLUNAS + """
                FROM public.agendamentos
                WHERE id = CAST(? AS UUID)
                """;

        return jdbcTemplate.query(sql, agendamentoMapper, id)
                .stream()
                .findFirst();
    }

    public List<Agendamento> listar(String clienteId, String barbeiroId,
                                    OffsetDateTime de, OffsetDateTime ate,
                                    String status) {

        String sql = "SELECT " + COLUNAS + """
                FROM public.agendamentos
                WHERE (CAST(? AS TEXT) IS NULL OR cliente_id = CAST(? AS UUID))
                  AND (CAST(? AS TEXT) IS NULL OR barbeiro_id = CAST(? AS UUID))
                  AND (CAST(? AS TIMESTAMPTZ) IS NULL OR inicio >= CAST(? AS TIMESTAMPTZ))
                  AND (CAST(? AS TIMESTAMPTZ) IS NULL OR inicio < CAST(? AS TIMESTAMPTZ))
                  AND (CAST(? AS TEXT) IS NULL OR status = CAST(? AS TEXT))
                ORDER BY inicio
                """;

        return jdbcTemplate.query(sql, agendamentoMapper,
                clienteId, clienteId,
                barbeiroId, barbeiroId,
                de, de,
                ate, ate,
                status, status);
    }

    public boolean mudarStatus(String id, String novoStatus,
                               String motivo, String statusAtual) {

        String sql = """
                UPDATE public.agendamentos
                SET status = ?,
                    motivo_cancelamento = ?
                WHERE id = CAST(? AS UUID)
                  AND status = ?
                """;

        return jdbcTemplate.update(sql, novoStatus, motivo, id, statusAtual) > 0;
    }
}