package br.edu.faculdade.barbearia;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BarbeiroPorServicoService {

    private final BarbeiroPorServicoRepository repositorio;
    private final AgendamentoRepository agendamentos;

    public BarbeiroPorServicoService(BarbeiroPorServicoRepository repositorio,
                                     AgendamentoRepository agendamentos) {
        this.repositorio = repositorio;
        this.agendamentos = agendamentos;
    }

    public List<BarbeiroResumo> listar(String servicoId) {

        ServicoAgendavel servico = agendamentos.servico(servicoId)
                .orElseThrow(() -> new RegraDeNegocioException(
                        HttpStatus.NOT_FOUND, "Serviço não encontrado."));

        if (!servico.ativo()) {
            throw new RegraDeNegocioException(HttpStatus.CONFLICT,
                    "Este serviço está inativo.");
        }

        return repositorio.doServico(servicoId);
    }
}