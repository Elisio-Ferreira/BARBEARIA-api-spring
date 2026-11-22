package br.edu.faculdade.barbearia;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record PagamentoEntrada(

        @Positive(message = "O valor deve ser maior que zero")
        BigDecimal valor,

        @NotBlank(message = "A forma de pagamento é obrigatória")
        String formaPagamento,

        @Size(max = 50, message = "A referência pode ter no máximo 50 caracteres")
        String referencia
) {
}