package br.edu.faculdade.barbearia;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

@Service
public class RelatorioService {

    private static final long MAX_DIAS = 366;

    private static final Set<String> STATUS_VALIDOS =
            Set.of("agendado", "confirmado", "cancelado", "concluido", "falta");

    private final RelatorioRepository repositorio;
    private final BarbeiroRepository barbeiros;

    public RelatorioService(RelatorioRepository repositorio,
                            BarbeiroRepository barbeiros) {
        this.repositorio = repositorio;
        this.barbeiros = barbeiros;
    }

    // ---------- Agenda ----------

    public List<AgendaItem> agenda(String barbeiroId, LocalDate de,
                                   LocalDate ate, String status) {

        LocalDate fimPeriodo = ate == null ? de : ate;
        validarPeriodo(de, fimPeriodo);
        exigirBarbeiro(barbeiroId);

        String statusNormalizado = null;
        if (status != null && !status.isBlank()) {
            statusNormalizado = status.trim().toLowerCase();
            if (!STATUS_VALIDOS.contains(statusNormalizado)) {
                throw new RegraDeNegocioException(HttpStatus.BAD_REQUEST,
                        "Status inválido. Use: agendado, confirmado, "
                                + "cancelado, concluido ou falta.");
            }
        }

        return repositorio.agenda(barbeiroId, inicio(de),
                fimExclusivo(fimPeriodo), statusNormalizado);
    }

    // ---------- Atendimentos ----------

    public RelatorioAtendimentos atendimentos(String barbeiroId,
                                              LocalDate de, LocalDate ate) {
        validarPeriodo(de, ate);
        exigirBarbeiro(barbeiroId);

        OffsetDateTime inicio = inicio(de);
        OffsetDateTime fim = fimExclusivo(ate);

        List<LinhaResumo> porBarbeiro =
                repositorio.atendimentosPorBarbeiro(barbeiroId, inicio, fim);
        List<LinhaResumo> porServico =
                repositorio.atendimentosPorServico(barbeiroId, inicio, fim);

        return new RelatorioAtendimentos(de, ate,
                quantidade(porBarbeiro), valor(porBarbeiro),
                porBarbeiro, porServico);
    }

    // ---------- Faturamento ----------

    public RelatorioFaturamento faturamento(String barbeiroId,
                                            LocalDate de, LocalDate ate) {
        validarPeriodo(de, ate);
        exigirBarbeiro(barbeiroId);

        OffsetDateTime inicio = inicio(de);
        OffsetDateTime fim = fimExclusivo(ate);

        List<LinhaResumo> porForma =
                repositorio.faturamentoPorForma(barbeiroId, inicio, fim);
        List<LinhaResumo> porBarbeiro =
                repositorio.faturamentoPorBarbeiro(barbeiroId, inicio, fim);
        BigDecimal estornado =
                repositorio.totalEstornado(barbeiroId, inicio, fim);

        return new RelatorioFaturamento(de, ate,
                quantidade(porForma), valor(porForma), estornado,
                porForma, porBarbeiro);
    }

    // ---------- Regras internas ----------

    private void validarPeriodo(LocalDate de, LocalDate ate) {
        if (ate.isBefore(de)) {
            throw new RegraDeNegocioException(HttpStatus.BAD_REQUEST,
                    "A data final deve ser igual ou depois da data inicial.");
        }
        if (ChronoUnit.DAYS.between(de, ate) + 1 > MAX_DIAS) {
            throw new RegraDeNegocioException(HttpStatus.BAD_REQUEST,
                    "O período máximo é de " + MAX_DIAS + " dias.");
        }
    }

    private void exigirBarbeiro(String barbeiroId) {
        if (barbeiroId != null && !barbeiros.existe(barbeiroId)) {
            throw new RegraDeNegocioException(
                    HttpStatus.NOT_FOUND, "Barbeiro não encontrado.");
        }
    }

    private OffsetDateTime inicio(LocalDate de) {
        return de.atStartOfDay(Fuso.RECIFE).toOffsetDateTime();
    }

    // "ate" é inclusivo: vai até o começo do dia seguinte
    private OffsetDateTime fimExclusivo(LocalDate ate) {
        return ate.plusDays(1).atStartOfDay(Fuso.RECIFE).toOffsetDateTime();
    }

    private long quantidade(List<LinhaResumo> linhas) {
        return linhas.stream().mapToLong(LinhaResumo::quantidade).sum();
    }

    private BigDecimal valor(List<LinhaResumo> linhas) {
        return linhas.stream()
                .map(LinhaResumo::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}