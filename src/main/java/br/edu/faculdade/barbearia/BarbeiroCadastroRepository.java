package br.edu.faculdade.barbearia;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class BarbeiroCadastroRepository {

    private final JdbcTemplate jdbcTemplate;

    public BarbeiroCadastroRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** Transforma o usuário recém-criado (cliente) em barbeiro. Devolve o id do barbeiro. */
    @Transactional
    public String converterClienteEmBarbeiro(String perfilId, String endereco) {

        jdbcTemplate.update("""
                DELETE FROM public.clientes
                WHERE perfil_id = CAST(? AS UUID)
                """, perfilId);

        int perfis = jdbcTemplate.update("""
                UPDATE public.perfis
                SET papel = 'barbeiro'
                WHERE id = CAST(? AS UUID)
                """, perfilId);

        if (perfis == 0) {
            throw new IllegalStateException("Perfil não encontrado: " + perfilId);
        }

        return jdbcTemplate.queryForObject("""
                INSERT INTO public.barbeiros (perfil_id, endereco, ativo)
                VALUES (CAST(? AS UUID), ?, true)
                RETURNING CAST(id AS TEXT)
                """, String.class, perfilId, endereco);
    }
}