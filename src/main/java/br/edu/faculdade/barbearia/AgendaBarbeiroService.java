package br.edu.faculdade.barbearia;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class AgendaBarbeiroService {

    private final AgendaBarbeiroRepository repositorio;
    private final BarbeiroRepository barbeiros;

    public AgendaBarbeiroService(AgendaBarbeiroRepository repositorio,
                                 BarbeiroRepository barbeiros) {
        this.repositorio = repositorio;
        this.barbeiros = barbeiros;
    }

    private void exigirBarbeiro(String barbeiroId) {
        if (!barbeiros.existe(barbeiroId)) {
            throw new RegraDeNegocioException(
                    HttpStatus.NOT_FOUND, "Barbeiro não encontrado.");
        }
    }

    // ---------- Horários de trabalho ----------

    public List<HorarioTrabalho> listarHorarios(String barbeiroId) {
        exigirBarbeiro(barbeiroId);
        return repositorio.horarios(barbeiroId);
    }

    @Transactional
    public List<HorarioTrabalho> definirHorarios(String barbeiroId,
                                                 List<HorarioTrabalhoEntrada> horarios) {
        exigirBarbeiro(barbeiroId);

        List<HorarioTrabalhoEntrada> ordenados = horarios.stream()
                .sorted(Comparator
                        .comparing(HorarioTrabalhoEntrada::diaSemana)
                        .thenComparing(HorarioTrabalhoEntrada::horaInicio))
                .toList();

        for (int i = 0; i < ordenados.size(); i++) {
            HorarioTrabalhoEntrada atual = ordenados.get(i);

            if (!atual.horaFim().isAfter(atual.horaInicio())) {
                throw new RegraDeNegocioException(HttpStatus.BAD_REQUEST,
                        "No dia " + atual.diaSemana()
                                + ", a hora de fim deve ser maior que a de início.");
            }

            if (i > 0) {
                HorarioTrabalhoEntrada anterior = ordenados.get(i - 1);
                boolean mesmoDia = anterior.diaSemana().equals(atual.diaSemana());
                if (mesmoDia && atual.horaInicio().isBefore(anterior.horaFim())) {
                    throw new RegraDeNegocioException(HttpStatus.BAD_REQUEST,
                            "No dia " + atual.diaSemana()
                                    + ", existem horários que se sobrepõem.");
                }
            }
        }

        repositorio.substituirHorarios(barbeiroId, ordenados);
        return repositorio.horarios(barbeiroId);
    }

    // ---------- Bloqueios ----------

    public List<Bloqueio> listarBloqueios(String barbeiroId) {
        exigirBarbeiro(barbeiroId);
        return repositorio.bloqueios(barbeiroId);
    }

    public Bloqueio criarBloqueio(String barbeiroId, BloqueioEntrada entrada) {
        exigirBarbeiro(barbeiroId);

        OffsetDateTime inicio = entrada.inicio().atZone(Fuso.RECIFE).toOffsetDateTime();
        OffsetDateTime fim = entrada.fim().atZone(Fuso.RECIFE).toOffsetDateTime();

        if (!fim.isAfter(inicio)) {
            throw new RegraDeNegocioException(HttpStatus.BAD_REQUEST,
                    "O fim do bloqueio deve ser depois do início.");
        }

        if (repositorio.contarAgendamentosAtivos(barbeiroId, inicio, fim) > 0) {
            throw new RegraDeNegocioException(HttpStatus.CONFLICT,
                    "Existem agendamentos nesse período. Cancele-os antes de bloquear.");
        }

        String motivo = (entrada.motivo() == null || entrada.motivo().isBlank())
                ? null : entrada.motivo().trim();

        return repositorio.criarBloqueio(barbeiroId, inicio, fim, motivo);
    }

    public void removerBloqueio(String barbeiroId, String bloqueioId) {
        exigirBarbeiro(barbeiroId);

        if (!repositorio.removerBloqueio(barbeiroId, bloqueioId)) {
            throw new RegraDeNegocioException(
                    HttpStatus.NOT_FOUND, "Bloqueio não encontrado.");
        }
    }
}