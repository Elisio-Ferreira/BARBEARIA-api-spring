package br.edu.faculdade.barbearia;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Repository
public class RelatorioRepository {

    private final JdbcTemplate jdbcTemplate;

    public RelatorioRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static OffsetDateTime paraRecife(OffsetDateTime valor) {
        return valor.atZoneSameInstant(Fuso.RECIFE).toOffsetDateTime();
    }

    private final RowMapper<LinhaResumo> linhaMapper = (rs, rowNum) ->
            new LinhaResumo(
                    rs.getString("chave"),
                    rs.getString("nome"),
                    rs.getLong("quantidade"),
                    rs.getBigDecimal("valor")
            );

    private final RowMapper<AgendaItem> agendaMapper = (rs, rowNum) ->
            new AgendaItem(
                    rs.getString("id"),
                    paraRecife(rs.getObject("inicio", OffsetDateTime.class)),
                    paraRecife(rs.getObject("fim", OffsetDateTime.class)),
                    rs.getString("status"),
                    rs.getString("cliente_id"),
                    rs.getString("cliente_nome"),
                    rs.getString("cliente_telefone"),
                    rs.getString("barbeiro_id"),
                    rs.getString("barbeiro_nome"),
                    rs.getString("servico_nome"),
                    rs.getBigDecimal("preco_combinado")
            );

    // ---------- Agenda ----------

    public List<AgendaItem> agenda(String barbeiroId, OffsetDateTime inicio,
                                   OffsetDateTime fim, String status) {

        String sql = """
                SELECT a.id,
                       a.inicio,
                       a.fim,
                       a.status,
                       a.preco_combinado,
                       a.cliente_id,
                       pc.nome AS cliente_nome,
                       pc.telefone AS cliente_telefone,
                       a.barbeiro_id,
                       pb.nome AS barbeiro_nome,
                       s.nome AS servico_nome
                FROM public.agendamentos a
                INNER JOIN public.clientes c ON c.id = a.cliente_id
                INNER JOIN public.perfis pc ON pc.id = c.perfil_id
                INNER JOIN public.barbeiros b ON b.id = a.barbeiro_id
                INNER JOIN public.perfis pb ON pb.id = b.perfil_id
                INNER JOIN public.servicos s ON s.id = a.servico_id
                WHERE a.inicio >= CAST(? AS TIMESTAMPTZ)
                  AND a.inicio < CAST(? AS TIMESTAMPTZ)
                  AND (CAST(? AS TEXT) IS NULL OR a.barbeiro_id = CAST(? AS UUID))
                  AND ((CAST(? AS TEXT) IS NULL
                        AND a.status IN ('agendado', 'confirmado'))
                       OR a.status = CAST(? AS TEXT))
                ORDER BY a.inicio, pb.nome
                """;

        return jdbcTemplate.query(sql, agendaMapper,
                inicio, fim,
                barbeiroId, barbeiroId,
                status, status);
    }

    // ---------- Atendimentos ----------

    public List<LinhaResumo> atendimentosPorBarbeiro(String barbeiroId,
                                                     OffsetDateTime inicio,
                                                     OffsetDateTime fim) {

        String sql = """
                SELECT CAST(b.id AS TEXT) AS chave,
                       pb.nome AS nome,
                       COUNT(*) AS quantidade,
                       COALESCE(SUM(a.preco_combinado), 0) AS valor
                FROM public.atendimentos t
                INNER JOIN public.agendamentos a ON a.id = t.agendamento_id
                INNER JOIN public.barbeiros b ON b.id = a.barbeiro_id
                INNER JOIN public.perfis pb ON pb.id = b.perfil_id
                WHERE t.data_realizacao >= CAST(? AS TIMESTAMPTZ)
                  AND t.data_realizacao < CAST(? AS TIMESTAMPTZ)
                  AND (CAST(? AS TEXT) IS NULL OR a.barbeiro_id = CAST(? AS UUID))
                GROUP BY b.id, pb.nome
                ORDER BY quantidade DESC, pb.nome
                """;

        return jdbcTemplate.query(sql, linhaMapper,
                inicio, fim, barbeiroId, barbeiroId);
    }

    public List<LinhaResumo> atendimentosPorServico(String barbeiroId,
                                                    OffsetDateTime inicio,
                                                    OffsetDateTime fim) {

        String sql = """
                SELECT CAST(s.id AS TEXT) AS chave,
                       s.nome AS nome,
                       COUNT(*) AS quantidade,
                       COALESCE(SUM(a.preco_combinado), 0) AS valor
                FROM public.atendimentos t
                INNER JOIN public.agendamentos a ON a.id = t.agendamento_id
                INNER JOIN public.servicos s ON s.id = a.servico_id
                WHERE t.data_realizacao >= CAST(? AS TIMESTAMPTZ)
                  AND t.data_realizacao < CAST(? AS TIMESTAMPTZ)
                  AND (CAST(? AS TEXT) IS NULL OR a.barbeiro_id = CAST(? AS UUID))
                GROUP BY s.id, s.nome
                ORDER BY quantidade DESC, s.nome
                """;

        return jdbcTemplate.query(sql, linhaMapper,
                inicio, fim, barbeiroId, barbeiroId);
    }

    // ---------- Faturamento ----------

    public List<LinhaResumo> faturamentoPorForma(String barbeiroId,
                                                 OffsetDateTime inicio,
                                                 OffsetDateTime fim) {

        String sql = """
                SELECT p.forma_pagamento AS chave,
                       p.forma_pagamento AS nome,
                       COUNT(*) AS quantidade,
                       COALESCE(SUM(p.valor), 0) AS valor
                FROM public.pagamentos p
                INNER JOIN public.agendamentos a ON a.id = p.agendamento_id
                WHERE p.status = 'pago'
                  AND p.data_pagamento >= CAST(? AS TIMESTAMPTZ)
                  AND p.data_pagamento < CAST(? AS TIMESTAMPTZ)
                  AND (CAST(? AS TEXT) IS NULL OR a.barbeiro_id = CAST(? AS UUID))
                GROUP BY p.forma_pagamento
                ORDER BY valor DESC
                """;

        return jdbcTemplate.query(sql, linhaMapper,
                inicio, fim, barbeiroId, barbeiroId);
    }

    public List<LinhaResumo> faturamentoPorBarbeiro(String barbeiroId,
                                                    OffsetDateTime inicio,
                                                    OffsetDateTime fim) {

        String sql = """
                SELECT CAST(b.id AS TEXT) AS chave,
                       pb.nome AS nome,
                       COUNT(*) AS quantidade,
                       COALESCE(SUM(p.valor), 0) AS valor
                FROM public.pagamentos p
                INNER JOIN public.agendamentos a ON a.id = p.agendamento_id
                INNER JOIN public.barbeiros b ON b.id = a.barbeiro_id
                INNER JOIN public.perfis pb ON pb.id = b.perfil_id
                WHERE p.status = 'pago'
                  AND p.data_pagamento >= CAST(? AS TIMESTAMPTZ)
                  AND p.data_pagamento < CAST(? AS TIMESTAMPTZ)
                  AND (CAST(? AS TEXT) IS NULL OR a.barbeiro_id = CAST(? AS UUID))
                GROUP BY b.id, pb.nome
                ORDER BY valor DESC
                """;

        return jdbcTemplate.query(sql, linhaMapper,
                inicio, fim, barbeiroId, barbeiroId);
    }

    public BigDecimal totalEstornado(String barbeiroId,
                                     OffsetDateTime inicio,
                                     OffsetDateTime fim) {

        String sql = """
                SELECT COALESCE(SUM(p.valor), 0)
                FROM public.pagamentos p
                INNER JOIN public.agendamentos a ON a.id = p.agendamento_id
                WHERE p.status = 'estornado'
                  AND p.data_pagamento >= CAST(? AS TIMESTAMPTZ)
                  AND p.data_pagamento < CAST(? AS TIMESTAMPTZ)
                  AND (CAST(? AS TEXT) IS NULL OR a.barbeiro_id = CAST(? AS UUID))
                """;

        BigDecimal total = jdbcTemplate.queryForObject(sql, BigDecimal.class,
                inicio, fim, barbeiroId, barbeiroId);

        return total == null ? BigDecimal.ZERO : total;
    }
}