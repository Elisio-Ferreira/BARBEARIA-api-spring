package br.edu.faculdade.barbearia;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;

@Repository
public class RemarcacaoRepository {

    private final JdbcTemplate jdbcTemplate;

    public RemarcacaoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean remarcar(String id, OffsetDateTime inicio,
                            OffsetDateTime fim, String statusAtual) {

        String sql = """
                UPDATE public.agendamentos
                SET inicio = ?,
                    fim = ?,
                    status = 'agendado'
                WHERE id = CAST(? AS UUID)
                  AND status = ?
                """;

        return jdbcTemplate.update(sql, inicio, fim, id, statusAtual) > 0;
    }
}