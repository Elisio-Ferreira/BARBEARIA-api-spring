package br.edu.faculdade.barbearia;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class ClienteRepository {

    private final List<Cliente> clientes = new CopyOnWriteArrayList<>(List.of(
            new Cliente("1", "Carlos Henrique", "carlos@exemplo.com", "(81) 99999-0001", "Corte"),
            new Cliente("2", "Rafael Gomes", "rafael@exemplo.com", "(81) 99999-0002", "Barba"),
            new Cliente("3", "João Pedro", "joao@exemplo.com", "(81) 99999-0003", "Combo"),
            new Cliente("4", "Felipe Andrade", "felipe@exemplo.com", "(81) 99999-0004", "Corte"),
            new Cliente("5", "Gustavo Reis", "gustavo@exemplo.com", "(81) 99999-0005", "Tratamento"),
            new Cliente("6", "Matheus Duarte", "matheus@exemplo.com", "(81) 99999-0006", "Barba")
    ));

    public List<Cliente> todos() {
        return List.copyOf(clientes);
    }

    public Optional<Cliente> porId(String id) {
        return clientes.stream().filter(c -> c.id().equals(id)).findFirst();
    }

    public Cliente salvar(Cliente novo) {
        clientes.add(novo);
        return novo;
    }

    public Optional<Cliente> trocar(String id, Cliente novo) {
        for (int i = 0; i < clientes.size(); i++) {
            if (clientes.get(i).id().equals(id)) {
                clientes.set(i, novo);
                return Optional.of(novo);
            }
        }
        return Optional.empty();
    }

    public boolean remover(String id) {
        return clientes.removeIf(c -> c.id().equals(id));
    }
}