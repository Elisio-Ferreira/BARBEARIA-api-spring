package br.edu.faculdade.barbearia;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Sabe onde o dado está. Hoje é uma lista em memória; quando entrar banco de
 * dados, só esta classe muda.
 *
 * Guarda Servico (o modelo), nunca ServicoResposta.
 *
 * Doze serviços, quatro categorias, quatro em promoção, cinco barbeiros: a
 * variedade é proposital, para que busca e estatísticas mostrem diferença.
 * Todo barbeiroSlug daqui PRECISA existir no BarbeiroRepository, escrito igual.
 *
 * CopyOnWriteArrayList: List.of é imutável, e ArrayList não é seguro quando
 * várias requisições mexem na lista ao mesmo tempo.
 */
@Repository
public class ServicoRepository {

    private final List<Servico> servicos = new CopyOnWriteArrayList<>(List.of(
        new Servico("1", "Corte Degradê", "Degradê na máquina com acabamento na navalha.",
            "Corte", 45.0, 45, false, "marcos-fade"),
        new Servico("2", "Corte Social", "Corte clássico na tesoura, pescoço e costeletas alinhados.",
            "Corte", 35.0, 30, false, "tiago-classico"),
        new Servico("3", "Corte Infantil", "Corte para crianças até 10 anos, com paciência de sobra.",
            "Corte", 30.0, 30, true, "dudu-tesoura"),
        new Servico("4", "Corte Navalhado", "Corte inteiro na navalha, do contorno ao degradê.",
            "Corte", 50.0, 50, false, "leo-navalha"),
        new Servico("5", "Barba Completa", "Aparar, alinhar e finalizar com óleo.",
            "Barba", 30.0, 30, false, "leo-navalha"),
        new Servico("6", "Barba com Toalha Quente", "Toalha quente, espuma e navalha, o ritual completo.",
            "Barba", 40.0, 40, true, "tiago-classico"),
        new Servico("7", "Barba Desenhada", "Contorno marcado e desenho das linhas do rosto.",
            "Barba", 35.0, 35, false, "marcos-fade"),
        new Servico("8", "Combo Corte + Barba", "Corte à escolha e barba completa numa só cadeira.",
            "Combo", 70.0, 70, true, "marcos-fade"),
        new Servico("9", "Combo Pai e Filho", "Dois cortes seguidos, com desconto para a dupla.",
            "Combo", 60.0, 60, true, "dudu-tesoura"),
        new Servico("10", "Hidratação Capilar", "Tratamento de hidratação com massagem no couro cabeludo.",
            "Tratamento", 40.0, 30, false, "bia-barber"),
        new Servico("11", "Pigmentação de Barba", "Preenche falhas e realça a cor da barba.",
            "Tratamento", 55.0, 45, false, "bia-barber"),
        new Servico("12", "Sobrancelha na Navalha", "Limpeza e alinhamento da sobrancelha.",
            "Tratamento", 20.0, 15, false, "bia-barber")
    ));

    public List<Servico> todos() {
        return List.copyOf(servicos);
    }

    public Optional<Servico> porId(String id) {
        return servicos.stream().filter(s -> s.id().equals(id)).findFirst();
    }

    public Servico salvar(Servico novo) {
        servicos.add(novo);
        return novo;
    }

    public Optional<Servico> trocar(String id, Servico novo) {
        for (int i = 0; i < servicos.size(); i++) {
            if (servicos.get(i).id().equals(id)) {
                servicos.set(i, novo);
                return Optional.of(novo);
            }
        }
        return Optional.empty();
    }

    public boolean remover(String id) {
        return servicos.removeIf(s -> s.id().equals(id));
    }
}
