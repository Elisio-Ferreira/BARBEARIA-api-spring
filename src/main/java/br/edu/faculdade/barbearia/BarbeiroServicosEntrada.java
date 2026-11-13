package br.edu.faculdade.barbearia;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record BarbeiroServicosEntrada(

        @NotNull(message = "A lista de serviços é obrigatória")
        List<UUID> servicoIds
) {
}