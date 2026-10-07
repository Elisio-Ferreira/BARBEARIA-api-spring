package br.edu.faculdade.barbearia;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Mesma forma do ServicoRepository, com uma diferença: aqui se busca por slug.
 *
 * Os cinco slugs precisam bater, letra por letra, com o barbeiroSlug dos
 * serviços. Um erro de digitação aqui não quebra nada agora, só aparece
 * quando a API for juntar serviço e barbeiro.
 */
@Repository
public class BarbeiroRepository {

    private final List<Barbeiro> barbeiros = new CopyOnWriteArrayList<>(List.of(
            new Barbeiro("1", "Marcos Almeida", "marcos-fade", "Degradê e barba desenhada",
                    "Dez anos de cadeira, referência em fade e acabamento."),
            new Barbeiro("2", "Tiago Ribeiro", "tiago-classico", "Corte social e barba com toalha quente",
                    "O clássico: tesoura, navalha e conversa boa."),
            new Barbeiro("3", "Eduardo Lima", "dudu-tesoura", "Corte infantil e combos",
                    "Especialista em atender criança sem choro."),
            new Barbeiro("4", "Leonardo Pires", "leo-navalha", "Corte e barba na navalha",
                    "Trabalha só na navalha, do contorno ao acabamento."),
            new Barbeiro("5", "Beatriz Souza", "bia-barber", "Tratamentos e sobrancelha",
                    "Cuida da parte química e do detalhe: pigmentação, hidratação e design de sobrancelha.")
    ));

    public List<Barbeiro> todos() {
        return List.copyOf(barbeiros);
    }

    public Optional<Barbeiro> porSlug(String slug) {
        return barbeiros.stream().filter(b -> b.slug().equals(slug)).findFirst();
    }

    public Barbeiro salvar(Barbeiro novo) {
        barbeiros.add(novo);
        return novo;
    }

    public boolean remover(String slug) {
        return barbeiros.removeIf(b -> b.slug().equals(slug));
    }
}