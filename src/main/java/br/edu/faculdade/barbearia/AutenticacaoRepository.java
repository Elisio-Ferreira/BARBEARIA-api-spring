package br.edu.faculdade.barbearia;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class AutenticacaoRepository {

    private final JdbcTemplate jdbcTemplate;

    public AutenticacaoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<UsuarioAutenticado> mapper = (rs, rowNum) ->
            new UsuarioAutenticado(
                    rs.getString("id"),
                    rs.getString("nome"),
                    rs.getString("email"),
                    rs.getString("papel"),
                    rs.getBoolean("ativo"),
                    rs.getString("cliente_id"),
                    rs.getString("barbeiro_id")
            );

    public Optional<UsuarioAutenticado> buscarPorPerfil(String perfilId) {

        String sql = """
                SELECT p.id,
                       p.nome,
                       u.email,
                       p.papel,
                       p.ativo,
                       c.id AS cliente_id,
                       b.id AS barbeiro_id
                FROM public.perfis p
                INNER JOIN auth.users u ON u.id = p.id
                LEFT JOIN public.clientes c ON c.perfil_id = p.id
                LEFT JOIN public.barbeiros b ON b.perfil_id = p.id
                WHERE p.id = CAST(? AS UUID)
                """;

        return jdbcTemplate.query(sql, mapper, perfilId)
                .stream()
                .findFirst();
    }
}