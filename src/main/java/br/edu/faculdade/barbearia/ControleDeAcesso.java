package br.edu.faculdade.barbearia;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ControleDeAcesso {

    private static final String ADM = "administrador";
    private static final String BAR = "barbeiro";
    private static final String CLI = "cliente";

    private final AgendamentoRepository agendamentos;

    public ControleDeAcesso(AgendamentoRepository agendamentos) {
        this.agendamentos = agendamentos;
    }

    /** Cliente só pode agir sobre o próprio cadastro. */
    public void exigirCliente(UsuarioAutenticado usuario, String clienteId) {
        if (CLI.equals(usuario.papel()) && !clienteId.equals(usuario.clienteId())) {
            negar();
        }
    }

    /** Barbeiro só pode agir sobre a própria agenda. */
    public void exigirBarbeiro(UsuarioAutenticado usuario, String barbeiroId) {
        if (BAR.equals(usuario.papel()) && !barbeiroId.equals(usuario.barbeiroId())) {
            negar();
        }
    }

    /** Cliente só vê agendamento dele; barbeiro, só os da sua agenda. */
    public void exigirAgendamento(UsuarioAutenticado usuario, String agendamentoId) {
        if (ADM.equals(usuario.papel())) {
            return;
        }
        // Se o agendamento não existe, o service responde 404
        agendamentos.porId(agendamentoId).ifPresent(a -> {
            exigirCliente(usuario, a.clienteId());
            exigirBarbeiro(usuario, a.barbeiroId());
        });
    }

    /** Para listas: o cliente só enxerga os próprios registros. */
    public String filtroCliente(UsuarioAutenticado usuario, String pedido) {
        if (CLI.equals(usuario.papel())) {
            if (pedido != null && !pedido.equals(usuario.clienteId())) {
                negar();
            }
            return usuario.clienteId();
        }
        return pedido;
    }

    /** Para listas: o barbeiro só enxerga a própria agenda. */
    public String filtroBarbeiro(UsuarioAutenticado usuario, String pedido) {
        if (BAR.equals(usuario.papel())) {
            if (pedido != null && !pedido.equals(usuario.barbeiroId())) {
                negar();
            }
            return usuario.barbeiroId();
        }
        return pedido;
    }

    private void negar() {
        throw new RegraDeNegocioException(HttpStatus.FORBIDDEN,
                "Você não tem acesso a este registro.");
    }
}