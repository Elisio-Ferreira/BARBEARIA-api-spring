package br.edu.faculdade.barbearia;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;

@Repository
public class AgendaBarbeiroRepository {

    private final JdbcTemplate jdbcTemplate;

    public AgendaBarbeiroRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static OffsetDateTime paraRecife(OffsetDateTime valor) {
        return valor.atZoneSameInstant(Fuso.RECIFE).toOffsetDateTime();
    }

    private final RowMapper<HorarioTrabalho> horarioMapper = (rs, rowNum) ->
            new HorarioTrabalho(
                    rs.getString("id"),
                    rs.getInt("dia_semana"),
                    rs.getObject("hora_inicio", LocalTime.class),
                    rs.getObject("hora_fim", LocalTime.class),
                    rs.getBoolean("ativo")
            );

    private final RowMapper<Bloqueio> bloqueioMapper = (rs, rowNum) ->
            new Bloqueio(
                    rs.getString("id"),
                    rs.getString("barbeiro_id"),
                    paraRecife(rs.getObject("inicio", OffsetDateTime.class)),
                    paraRecife(rs.getObject("fim", OffsetDateTime.class)),
                    rs.getString("motivo")
            );

    // ---------- Horários de trabalho ----------

    public List<HorarioTrabalho> horarios(String barbeiroId) {

        String sql = """
                SELECT id, dia_semana, hora_inicio, hora_fim, ativo
                FROM public.horarios_trabalho
                WHERE barbeiro_id = CAST(? AS UUID)
                ORDER BY dia_semana, hora_inicio
                """;

        return jdbcTemplate.query(sql, horarioMapper, barbeiroId);
    }

    public void substituirHorarios(String barbeiroId,
                                   List<HorarioTrabalhoEntrada> horarios) {

        jdbcTemplate.update(
                "DELETE FROM public.horarios_trabalho WHERE barbeiro_id = CAST(? AS UUID)",
                barbeiroId
        );

        for (HorarioTrabalhoEntrada h : horarios) {
            jdbcTemplate.update("""
                    INSERT INTO public.horarios_trabalho
                        (barbeiro_id, dia_semana, hora_inicio, hora_fim, ativo)
                    VALUES (CAST(? AS UUID), ?, ?, ?, true)
                    """,
                    barbeiroId, h.diaSemana(), h.horaInicio(), h.horaFim());
        }
    }

    // ---------- Bloqueios ----------

    public List<Bloqueio> bloqueios(String barbeiroId) {

        String sql = """
                SELECT id, barbeiro_id, inicio, fim, motivo
                FROM public.bloqueios_agenda
                WHERE barbeiro_id = CAST(? AS UUID)
                ORDER BY inicio
                """;

        return jdbcTemplate.query(sql, bloqueioMapper, barbeiroId);
    }

    public Bloqueio criarBloqueio(String barbeiroId, OffsetDateTime inicio,
                                  OffsetDateTime fim, String motivo) {

        String sql = """
                INSERT INTO public.bloqueios_agenda (barbeiro_id, inicio, fim, motivo)
                VALUES (CAST(? AS UUID), ?, ?, ?)
                RETURNING id, barbeiro_id, inicio, fim, motivo
                """;

        return jdbcTemplate.queryForObject(sql, bloqueioMapper,
                barbeiroId, inicio, fim, motivo);
    }

    public boolean removerBloqueio(String barbeiroId, String bloqueioId) {

        String sql = """
                DELETE FROM public.bloqueios_agenda
                WHERE id = CAST(? AS UUID)
                  AND barbeiro_id = CAST(? AS UUID)
                """;

        return jdbcTemplate.update(sql, bloqueioId, barbeiroId) > 0;
    }

    public int contarAgendamentosAtivos(String barbeiroId,
                                        OffsetDateTime inicio,
                                        OffsetDateTime fim) {

        String sql = """
                SELECT COUNT(*)
                FROM public.agendamentos
                WHERE barbeiro_id = CAST(? AS UUID)
                  AND status IN ('agendado', 'confirmado')
                  AND inicio < CAST(? AS TIMESTAMPTZ)
                  AND fim > CAST(? AS TIMESTAMPTZ)
                """;

        Integer total = jdbcTemplate.queryForObject(
                sql, Integer.class, barbeiroId, fim, inicio);

        return total == null ? 0 : total;
    }
}