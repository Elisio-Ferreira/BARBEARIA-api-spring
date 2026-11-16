package br.edu.faculdade.barbearia;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

@Service
public class PagamentoService {

    private static final Set<String> FORMAS = Set.of(
            "dinheiro", "pix", "cartao_credito", "cartao_debito", "outro");

    private final PagamentoRepository repositorio;
    private final AgendamentoRepository agendamentos;

    public PagamentoService(PagamentoRepository repositorio,
                            AgendamentoRepository agendamentos) {
        this.repositorio = repositorio;
        this.agendamentos = agendamentos;
    }

    @Transactional
    public Pagamento registrar(String agendamentoId, PagamentoEntrada entrada) {

        String forma = entrada.formaPagamento().trim().toLowerCase();
        if (!FORMAS.contains(forma)) {
            throw new RegraDeNegocioException(HttpStatus.BAD_REQUEST,
                    "Forma de pagamento inválida. Use: dinheiro, pix, "
                            + "cartao_credito, cartao_debito ou outro.");
        }

        Agendamento a = exigirAgendamento(agendamentoId);

        if (!"concluido".equals(a.status())) {
            throw new RegraDeNegocioException(HttpStatus.CONFLICT,
                    "Só é possível registrar pagamento de um agendamento concluído.");
        }

        BigDecimal pago = repositorio.totalPago(agendamentoId);
        BigDecimal restante = a.precoCombinado().subtract(pago);

        if (restante.signum() <= 0) {
            throw new RegraDeNegocioException(HttpStatus.CONFLICT,
                    "Este agendamento já está totalmente pago.");
        }

        BigDecimal valor = entrada.valor() == null ? restante : entrada.valor();

        if (valor.compareTo(restante) > 0) {
            throw new RegraDeNegocioException(HttpStatus.CONFLICT,
                    "O valor excede o saldo restante de R$ "
                            + restante.toPlainString() + ".");
        }

        String referencia = (entrada.referencia() == null
                || entrada.referencia().isBlank())
                ? null : entrada.referencia().trim();

        return repositorio.inserir(agendamentoId, valor, forma,
                OffsetDateTime.now(Fuso.RECIFE), referencia);
    }

    public List<Pagamento> listar(String agendamentoId) {
        exigirAgendamento(agendamentoId);
        return repositorio.doAgendamento(agendamentoId);
    }

    public Pagamento estornar(String id) {

        Pagamento p = repositorio.porId(id)
                .orElseThrow(() -> new RegraDeNegocioException(
                        HttpStatus.NOT_FOUND, "Pagamento não encontrado."));

        if (!"pago".equals(p.status())) {
            throw new RegraDeNegocioException(HttpStatus.CONFLICT,
                    "Não é possível estornar um pagamento com situação '"
                            + p.status() + "'.");
        }

        if (!repositorio.mudarStatus(id, "estornado", "pago")) {
            throw new RegraDeNegocioException(HttpStatus.CONFLICT,
                    "O pagamento foi alterado por outra operação. "
                            + "Consulte-o novamente.");
        }

        return repositorio.porId(id)
                .orElseThrow(() -> new RegraDeNegocioException(
                        HttpStatus.NOT_FOUND, "Pagamento não encontrado."));
    }

    private Agendamento exigirAgendamento(String id) {
        return agendamentos.porId(id)
                .orElseThrow(() -> new RegraDeNegocioException(
                        HttpStatus.NOT_FOUND, "Agendamento não encontrado."));
    }
}