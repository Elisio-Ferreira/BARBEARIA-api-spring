package br.edu.faculdade.barbearia;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class PagamentoRepository {

    private static final String COLUNAS = """
            id, agendamento_id, valor, forma_pagamento, status,
            data_pagamento, referencia
            """;

    private final JdbcTemplate jdbcTemplate;

    public PagamentoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Pagamento> pagamentoMapper = (rs, rowNum) -> {
        OffsetDateTime data = rs.getObject("data_pagamento", OffsetDateTime.class);

        return new Pagamento(
                rs.getString("id"),
                rs.getString("agendamento_id"),
                rs.getBigDecimal("valor"),
                rs.getString("forma_pagamento"),
                rs.getString("status"),
                data == null ? null
                        : data.atZoneSameInstant(Fuso.RECIFE).toOffsetDateTime(),
                rs.getString("referencia")
        );
    };

    public Pagamento inserir(String agendamentoId, BigDecimal valor,
                             String formaPagamento, OffsetDateTime dataPagamento,
                             String referencia) {

        String sql = """
                INSERT INTO public.pagamentos
                    (agendamento_id, valor, forma_pagamento, status,
                     data_pagamento, referencia)
                VALUES (CAST(? AS UUID), ?, ?, 'pago', ?, ?)
                RETURNING
                """ + COLUNAS;

        return jdbcTemplate.queryForObject(sql, pagamentoMapper,
                agendamentoId, valor, formaPagamento, dataPagamento, referencia);
    }

    public Optional<Pagamento> porId(String id) {

        String sql = "SELECT " + COLUNAS + """
                FROM public.pagamentos
                WHERE id = CAST(? AS UUID)
                """;

        return jdbcTemplate.query(sql, pagamentoMapper, id)
                .stream()
                .findFirst();
    }

    public List<Pagamento> doAgendamento(String agendamentoId) {

        String sql = "SELECT " + COLUNAS + """
                FROM public.pagamentos
                WHERE agendamento_id = CAST(? AS UUID)
                ORDER BY created_at
                """;

        return jdbcTemplate.query(sql, pagamentoMapper, agendamentoId);
    }

    public BigDecimal totalPago(String agendamentoId) {

        String sql = """
                SELECT COALESCE(SUM(valor), 0)
                FROM public.pagamentos
                WHERE agendamento_id = CAST(? AS UUID)
                  AND status = 'pago'
                """;

        BigDecimal total = jdbcTemplate.queryForObject(
                sql, BigDecimal.class, agendamentoId);

        return total == null ? BigDecimal.ZERO : total;
    }

    public boolean mudarStatus(String id, String novoStatus, String statusAtual) {

        String sql = """
                UPDATE public.pagamentos
                SET status = ?
                WHERE id = CAST(? AS UUID)
                  AND status = ?
                """;

        return jdbcTemplate.update(sql, novoStatus, id, statusAtual) > 0;
    }
}