
package br.edu.faculdade.barbearia;

public record AuthResposta(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        String userId,
        String email
) {
}
