package br.edu.faculdade.barbearia;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class RemarcacaoService {

    private static final int INTERVALO_MINUTOS = 30;
    private static final int ANTECEDENCIA_MINUTOS = 60;

    private final RemarcacaoRepository repositorio;
    private final AgendamentoRepository agendamentos;
    private final AgendaBarbeiroRepository agenda;
    private final BarbeiroRepository barbeiros;

    public RemarcacaoService(RemarcacaoRepository repositorio,
                             AgendamentoRepository agendamentos,
                             AgendaBarbeiroRepository agenda,
                             BarbeiroRepository barbeiros) {
        this.repositorio = repositorio;
        this.agendamentos = agendamentos;
        this.agenda = agenda;
        this.barbeiros = barbeiros;
    }

    public Agendamento remarcar(String id, LocalDateTime novoInicioLocal) {

        Agendamento a = agendamentos.porId(id)
                .orElseThrow(() -> new RegraDeNegocioException(
                        HttpStatus.NOT_FOUND, "Agendamento não encontrado."));

        if (!"agendado".equals(a.status()) && !"confirmado".equals(a.status())) {
            throw new RegraDeNegocioException(HttpStatus.CONFLICT,
                    "Não é possível remarcar um agendamento com situação '"
                            + a.status() + "'.");
        }

        Barbeiro barbeiro = barbeiros.porId(a.barbeiroId())
                .orElseThrow(() -> new RegraDeNegocioException(
                        HttpStatus.NOT_FOUND, "Barbeiro não encontrado."));

        if (!barbeiro.ativo()) {
            throw new RegraDeNegocioException(HttpStatus.CONFLICT,
                    "Este barbeiro está inativo.");
        }

        OffsetDateTime novoInicio =
                novoInicioLocal.atZone(Fuso.RECIFE).toOffsetDateTime();

        OffsetDateTime minimo = OffsetDateTime.now(Fuso.RECIFE)
                .plusMinutes(ANTECEDENCIA_MINUTOS);
        if (novoInicio.isBefore(minimo)) {
            throw new RegraDeNegocioException(HttpStatus.BAD_REQUEST,
                    "O agendamento precisa ser feito com pelo menos 1 hora de antecedência.");
        }

        if (novoInicio.isEqual(a.inicio())) {
            throw new RegraDeNegocioException(HttpStatus.BAD_REQUEST,
                    "O novo horário é igual ao horário atual do agendamento.");
        }

        List<LocalDateTime> livres = horariosLivres(a, novoInicioLocal.toLocalDate());

        if (!livres.contains(novoInicioLocal)) {
            throw new RegraDeNegocioException(HttpStatus.CONFLICT,
                    "Horário indisponível para este barbeiro. "
                            + "Consulte /agendamentos/disponibilidade.");
        }

        OffsetDateTime novoFim = novoInicio.plusMinutes(a.duracaoMinutos());

        boolean ok;
        try {
            ok = repositorio.remarcar(id, novoInicio, novoFim, a.status());
        } catch (DataIntegrityViolationException ex) {
            // A trava do banco pegou um conflito de última hora
            throw new RegraDeNegocioException(HttpStatus.CONFLICT,
                    "Esse horário acabou de ser ocupado por outro agendamento.");
        }

        if (!ok) {
            throw new RegraDeNegocioException(HttpStatus.CONFLICT,
                    "O agendamento foi alterado por outra operação. Consulte-o novamente.");
        }

        return agendamentos.porId(id)
                .orElseThrow(() -> new RegraDeNegocioException(
                        HttpStatus.NOT_FOUND, "Agendamento não encontrado."));
    }

    // Mesma lógica da disponibilidade, mas ignorando o próprio agendamento
    private List<LocalDateTime> horariosLivres(Agendamento a, LocalDate data) {

        // 0 = domingo ... 6 = sábado (DayOfWeek: segunda = 1 ... domingo = 7)
        int diaSemana = data.getDayOfWeek().getValue() % 7;

        List<HorarioTrabalho> janelas = agenda.horarios(a.barbeiroId()).stream()
                .filter(h -> h.ativo() && h.diaSemana() == diaSemana)
                .toList();

        if (janelas.isEmpty()) {
            return List.of();
        }

        OffsetDateTime inicioDia = data.atStartOfDay(Fuso.RECIFE).toOffsetDateTime();
        OffsetDateTime fimDia = data.plusDays(1).atStartOfDay(Fuso.RECIFE).toOffsetDateTime();

        List<Bloqueio> bloqueios = agenda.bloqueios(a.barbeiroId()).stream()
                .filter(b -> b.inicio().isBefore(fimDia) && b.fim().isAfter(inicioDia))
                .toList();

        List<Agendamento> ocupados = agendamentos
                .ocupados(a.barbeiroId(), inicioDia, fimDia).stream()
                .filter(o -> !o.id().equals(a.id()))
                .toList();

        int duracao = a.duracaoMinutos();
        List<LocalDateTime> livres = new ArrayList<>();

        for (HorarioTrabalho janela : janelas) {
            LocalDateTime fimJanela = data.atTime(janela.horaFim());

            for (LocalDateTime inicio = data.atTime(janela.horaInicio());
                 !inicio.plusMinutes(duracao).isAfter(fimJanela);
                 inicio = inicio.plusMinutes(INTERVALO_MINUTOS)) {

                OffsetDateTime ini = inicio.atZone(Fuso.RECIFE).toOffsetDateTime();
                OffsetDateTime fim = ini.plusMinutes(duracao);

                boolean batePeloBloqueio = bloqueios.stream()
                        .anyMatch(b -> ini.isBefore(b.fim()) && fim.isAfter(b.inicio()));

                boolean batePeloAgendamento = ocupados.stream()
                        .anyMatch(o -> ini.isBefore(o.fim()) && fim.isAfter(o.inicio()));

                if (!batePeloBloqueio && !batePeloAgendamento) {
                    livres.add(inicio);
                }
            }
        }

        return livres;
    }
}