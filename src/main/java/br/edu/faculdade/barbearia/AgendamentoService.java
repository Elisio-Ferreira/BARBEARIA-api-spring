package br.edu.faculdade.barbearia;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class AgendamentoService {

    private static final int INTERVALO_MINUTOS = 30;
    private static final int ANTECEDENCIA_MINUTOS = 60;

    private static final Set<String> STATUS_VALIDOS =
            Set.of("agendado", "confirmado", "cancelado", "concluido", "falta");

    private final AgendamentoRepository repositorio;
    private final AgendaBarbeiroRepository agenda;
    private final BarbeiroRepository barbeiros;
    private final ClienteRepository clientes;

    public AgendamentoService(AgendamentoRepository repositorio,
                              AgendaBarbeiroRepository agenda,
                              BarbeiroRepository barbeiros,
                              ClienteRepository clientes) {
        this.repositorio = repositorio;
        this.agenda = agenda;
        this.barbeiros = barbeiros;
        this.clientes = clientes;
    }

    // ---------- Disponibilidade ----------

    public List<HorarioDisponivel> disponibilidade(String barbeiroId,
                                                   String servicoId,
                                                   LocalDate data) {
        exigirBarbeiroAtivo(barbeiroId);
        ServicoAgendavel servico = exigirServicoDoBarbeiro(barbeiroId, servicoId);

        return calcularHorarios(barbeiroId, servico, data).stream()
                .map(inicio -> new HorarioDisponivel(
                        inicio, inicio.plusMinutes(servico.duracaoMinutos())))
                .toList();
    }

    // ---------- Criar agendamento ----------

    public Agendamento criar(AgendamentoEntrada entrada) {

        String clienteId = entrada.clienteId().toString();
        String barbeiroId = entrada.barbeiroId().toString();
        String servicoId = entrada.servicoId().toString();

        Cliente cliente = clientes.porId(clienteId)
                .orElseThrow(() -> new RegraDeNegocioException(
                        HttpStatus.NOT_FOUND, "Cliente não encontrado."));

        if (!cliente.ativo()) {
            throw new RegraDeNegocioException(HttpStatus.CONFLICT,
                    "Este cliente está inativo e não pode agendar.");
        }

        exigirBarbeiroAtivo(barbeiroId);
        ServicoAgendavel servico = exigirServicoDoBarbeiro(barbeiroId, servicoId);

        LocalDateTime inicioLocal = entrada.inicio();
        OffsetDateTime inicio = inicioLocal.atZone(Fuso.RECIFE).toOffsetDateTime();

        OffsetDateTime minimo = OffsetDateTime.now(Fuso.RECIFE)
                .plusMinutes(ANTECEDENCIA_MINUTOS);
        if (inicio.isBefore(minimo)) {
            throw new RegraDeNegocioException(HttpStatus.BAD_REQUEST,
                    "O agendamento precisa ser feito com pelo menos 1 hora de antecedência.");
        }

        List<LocalDateTime> livres = calcularHorarios(
                barbeiroId, servico, inicioLocal.toLocalDate());

        if (!livres.contains(inicioLocal)) {
            throw new RegraDeNegocioException(HttpStatus.CONFLICT,
                    "Horário indisponível para este barbeiro e serviço. "
                            + "Consulte /agendamentos/disponibilidade.");
        }

        OffsetDateTime fim = inicio.plusMinutes(servico.duracaoMinutos());

        try {
            return repositorio.inserir(clienteId, barbeiroId, servicoId,
                    inicio, fim, servico.preco(), servico.duracaoMinutos());
        } catch (DataIntegrityViolationException ex) {
            throw new RegraDeNegocioException(HttpStatus.CONFLICT,
                    "Esse horário acabou de ser ocupado por outro agendamento.");
        }
    }

    public Optional<Agendamento> buscarPorId(String id) {
        return repositorio.porId(id);
    }

    // ---------- Listar ----------

    public List<Agendamento> listar(String clienteId, String barbeiroId,
                                    LocalDate de, LocalDate ate, String status) {

        String statusNormalizado = null;
        if (status != null && !status.isBlank()) {
            statusNormalizado = status.trim().toLowerCase();
            if (!STATUS_VALIDOS.contains(statusNormalizado)) {
                throw new RegraDeNegocioException(HttpStatus.BAD_REQUEST,
                        "Status inválido. Use: agendado, confirmado, "
                                + "cancelado, concluido ou falta.");
            }
        }

        if (de != null && ate != null && ate.isBefore(de)) {
            throw new RegraDeNegocioException(HttpStatus.BAD_REQUEST,
                    "A data final deve ser igual ou depois da data inicial.");
        }

        OffsetDateTime inicio = de == null ? null
                : de.atStartOfDay(Fuso.RECIFE).toOffsetDateTime();
        // "ate" é inclusivo: vai até o começo do dia seguinte
        OffsetDateTime fim = ate == null ? null
                : ate.plusDays(1).atStartOfDay(Fuso.RECIFE).toOffsetDateTime();

        return repositorio.listar(clienteId, barbeiroId, inicio, fim, statusNormalizado);
    }

    // ---------- Mudança de situação ----------

    public Agendamento confirmar(String id) {
        Agendamento a = exigirAgendamento(id);
        exigirStatus(a, "confirmar", "agendado");
        return aplicar(a, "confirmado", null);
    }

    public Agendamento cancelar(String id, String motivo) {
        Agendamento a = exigirAgendamento(id);
        exigirStatus(a, "cancelar", "agendado", "confirmado");

        String m = (motivo == null || motivo.isBlank()) ? null : motivo.trim();
        return aplicar(a, "cancelado", m);
    }

    public Agendamento registrarFalta(String id) {
        Agendamento a = exigirAgendamento(id);
        exigirStatus(a, "registrar falta em", "agendado", "confirmado");
        exigirJaIniciado(a, "registrar falta");
        return aplicar(a, "falta", null);
    }

    public Agendamento concluir(String id) {
        Agendamento a = exigirAgendamento(id);
        exigirStatus(a, "concluir", "agendado", "confirmado");
        exigirJaIniciado(a, "concluir");
        return aplicar(a, "concluido", null);
    }

    private Agendamento exigirAgendamento(String id) {
        return repositorio.porId(id)
                .orElseThrow(() -> new RegraDeNegocioException(
                        HttpStatus.NOT_FOUND, "Agendamento não encontrado."));
    }

    private void exigirStatus(Agendamento a, String acao, String... permitidos) {
        for (String p : permitidos) {
            if (p.equals(a.status())) {
                return;
            }
        }
        throw new RegraDeNegocioException(HttpStatus.CONFLICT,
                "Não é possível " + acao + " um agendamento com situação '"
                        + a.status() + "'.");
    }

    private void exigirJaIniciado(Agendamento a, String acao) {
        if (a.inicio().isAfter(OffsetDateTime.now(Fuso.RECIFE))) {
            throw new RegraDeNegocioException(HttpStatus.CONFLICT,
                    "Não é possível " + acao
                            + " antes do horário de início do agendamento.");
        }
    }

    private Agendamento aplicar(Agendamento a, String novoStatus, String motivo) {
        boolean ok = repositorio.mudarStatus(a.id(), novoStatus, motivo, a.status());

        if (!ok) {
            throw new RegraDeNegocioException(HttpStatus.CONFLICT,
                    "O agendamento foi alterado por outra operação. Consulte-o novamente.");
        }

        return repositorio.porId(a.id())
                .orElseThrow(() -> new RegraDeNegocioException(
                        HttpStatus.NOT_FOUND, "Agendamento não encontrado."));
    }

    // ---------- Regras internas ----------

    private void exigirBarbeiroAtivo(String barbeiroId) {
        Barbeiro barbeiro = barbeiros.porId(barbeiroId)
                .orElseThrow(() -> new RegraDeNegocioException(
                        HttpStatus.NOT_FOUND, "Barbeiro não encontrado."));

        if (!barbeiro.ativo()) {
            throw new RegraDeNegocioException(HttpStatus.CONFLICT,
                    "Este barbeiro está inativo.");
        }
    }

    private ServicoAgendavel exigirServicoDoBarbeiro(String barbeiroId,
                                                     String servicoId) {
        ServicoAgendavel servico = repositorio.servico(servicoId)
                .orElseThrow(() -> new RegraDeNegocioException(
                        HttpStatus.NOT_FOUND, "Serviço não encontrado."));

        if (!servico.ativo()) {
            throw new RegraDeNegocioException(HttpStatus.CONFLICT,
                    "Este serviço está inativo.");
        }

        if (!repositorio.servicoVinculado(barbeiroId, servicoId)) {
            throw new RegraDeNegocioException(HttpStatus.BAD_REQUEST,
                    "Este barbeiro não realiza o serviço informado.");
        }

        return servico;
    }

    private List<LocalDateTime> calcularHorarios(String barbeiroId,
                                                 ServicoAgendavel servico,
                                                 LocalDate data) {
        // 0 = domingo ... 6 = sábado (DayOfWeek: segunda = 1 ... domingo = 7)
        int diaSemana = data.getDayOfWeek().getValue() % 7;

        List<HorarioTrabalho> janelas = agenda.horarios(barbeiroId).stream()
                .filter(h -> h.ativo() && h.diaSemana() == diaSemana)
                .toList();

        if (janelas.isEmpty()) {
            return List.of();
        }

        OffsetDateTime inicioDia = data.atStartOfDay(Fuso.RECIFE).toOffsetDateTime();
        OffsetDateTime fimDia = data.plusDays(1).atStartOfDay(Fuso.RECIFE).toOffsetDateTime();

        List<Bloqueio> bloqueios = agenda.bloqueios(barbeiroId).stream()
                .filter(b -> b.inicio().isBefore(fimDia) && b.fim().isAfter(inicioDia))
                .toList();

        List<Agendamento> ocupados = repositorio.ocupados(barbeiroId, inicioDia, fimDia);

        OffsetDateTime minimo = OffsetDateTime.now(Fuso.RECIFE)
                .plusMinutes(ANTECEDENCIA_MINUTOS);

        int duracao = servico.duracaoMinutos();
        List<LocalDateTime> livres = new ArrayList<>();

        for (HorarioTrabalho janela : janelas) {
            LocalDateTime fimJanela = data.atTime(janela.horaFim());

            for (LocalDateTime inicio = data.atTime(janela.horaInicio());
                 !inicio.plusMinutes(duracao).isAfter(fimJanela);
                 inicio = inicio.plusMinutes(INTERVALO_MINUTOS)) {

                OffsetDateTime ini = inicio.atZone(Fuso.RECIFE).toOffsetDateTime();
                OffsetDateTime fim = ini.plusMinutes(duracao);

                if (ini.isBefore(minimo)) {
                    continue;
                }

                boolean batePeloBloqueio = bloqueios.stream()
                        .anyMatch(b -> ini.isBefore(b.fim()) && fim.isAfter(b.inicio()));

                boolean batePeloAgendamento = ocupados.stream()
                        .anyMatch(a -> ini.isBefore(a.fim()) && fim.isAfter(a.inicio()));

                if (!batePeloBloqueio && !batePeloAgendamento) {
                    livres.add(inicio);
                }
            }
        }

        return livres;
    }
}