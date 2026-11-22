package br.edu.faculdade.barbearia;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

@Service
public class AtendimentoService {

    private static final Set<String> STATUS_PERMITIDOS =
            Set.of("agendado", "confirmado", "concluido");

    private final AtendimentoRepository repositorio;
    private final AgendamentoRepository agendamentos;

    public AtendimentoService(AtendimentoRepository repositorio,
                              AgendamentoRepository agendamentos) {
        this.repositorio = repositorio;
        this.agendamentos = agendamentos;
    }

    @Transactional
    public Atendimento registrar(String agendamentoId, String observacoes) {

        Agendamento a = exigirAgendamento(agendamentoId);

        if (!STATUS_PERMITIDOS.contains(a.status())) {
            throw new RegraDeNegocioException(HttpStatus.CONFLICT,
                    "Não é possível registrar atendimento de um agendamento "
                            + "com situação '" + a.status() + "'.");
        }

        if (a.inicio().isAfter(OffsetDateTime.now(Fuso.RECIFE))) {
            throw new RegraDeNegocioException(HttpStatus.CONFLICT,
                    "Não é possível registrar o atendimento antes do "
                            + "horário de início do agendamento.");
        }

        if (repositorio.existePorAgendamento(agendamentoId)) {
            throw jaExiste();
        }

        String obs = (observacoes == null || observacoes.isBlank())
                ? null : observacoes.trim();

        String novoId;
        try {
            novoId = repositorio.inserir(
                    agendamentoId, OffsetDateTime.now(Fuso.RECIFE), obs);
        } catch (DataIntegrityViolationException ex) {
            throw jaExiste();
        }

        if (!"concluido".equals(a.status())) {
            boolean ok = agendamentos.mudarStatus(
                    agendamentoId, "concluido", null, a.status());

            if (!ok) {
                throw new RegraDeNegocioException(HttpStatus.CONFLICT,
                        "O agendamento foi alterado por outra operação. "
                                + "Consulte-o novamente.");
            }
        }

        return repositorio.porId(novoId)
                .orElseThrow(() -> new RegraDeNegocioException(
                        HttpStatus.NOT_FOUND, "Atendimento não encontrado."));
    }

    public Atendimento porAgendamento(String agendamentoId) {
        exigirAgendamento(agendamentoId);

        return repositorio.porAgendamento(agendamentoId)
                .orElseThrow(() -> new RegraDeNegocioException(
                        HttpStatus.NOT_FOUND,
                        "Este agendamento ainda não tem atendimento registrado."));
    }

    public List<Atendimento> listar(String clienteId, String barbeiroId,
                                    LocalDate de, LocalDate ate) {

        if (de != null && ate != null && ate.isBefore(de)) {
            throw new RegraDeNegocioException(HttpStatus.BAD_REQUEST,
                    "A data final deve ser igual ou depois da data inicial.");
        }

        OffsetDateTime inicio = de == null ? null
                : de.atStartOfDay(Fuso.RECIFE).toOffsetDateTime();
        OffsetDateTime fim = ate == null ? null
                : ate.plusDays(1).atStartOfDay(Fuso.RECIFE).toOffsetDateTime();

        return repositorio.listar(clienteId, barbeiroId, inicio, fim);
    }

    private Agendamento exigirAgendamento(String id) {
        return agendamentos.porId(id)
                .orElseThrow(() -> new RegraDeNegocioException(
                        HttpStatus.NOT_FOUND, "Agendamento não encontrado."));
    }

    private RegraDeNegocioException jaExiste() {
        return new RegraDeNegocioException(HttpStatus.CONFLICT,
                "Já existe um atendimento registrado para este agendamento.");
    }
}