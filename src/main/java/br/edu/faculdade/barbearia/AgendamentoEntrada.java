package br.edu.faculdade.barbearia;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record AgendamentoEntrada(

        @NotNull(message = "O cliente é obrigatório")
        UUID clienteId,

        @NotNull(message = "O barbeiro é obrigatório")
        UUID barbeiroId,

        @NotNull(message = "O serviço é obrigatório")
        UUID servicoId,

        @NotNull(message = "O horário de início é obrigatório")
        LocalDateTime inicio
) {
}