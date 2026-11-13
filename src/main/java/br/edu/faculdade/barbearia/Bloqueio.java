package br.edu.faculdade.barbearia;

import java.time.OffsetDateTime;

public record Bloqueio(
        String id,
        String barbeiroId,
        OffsetDateTime inicio,
        OffsetDateTime fim,
        String motivo
) {
}