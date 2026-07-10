package br.com.sea.desafio.security;

import br.com.sea.desafio.domain.Role;
import br.com.sea.desafio.domain.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "chave-de-teste-para-tokens-jwt-desafio-sea-tecnologia-2026";

    @Test
    void geraEExtraiClaimsDoMesmoToken() {
        JwtService service = new JwtService(SECRET, 3600000);

        String token = service.gerarToken(new Usuario("admin", "hash", Role.ADMIN));
        Claims claims = service.extrairClaims(token);

        assertThat(claims.getSubject()).isEqualTo("admin");
        assertThat(claims.get("role", String.class)).isEqualTo("ADMIN");
        assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
    }

    @Test
    void rejeitaTokenExpirado() {
        JwtService service = new JwtService(SECRET, -1000); // já nasce expirado

        String token = service.gerarToken(new Usuario("user", "hash", Role.USER));

        assertThatThrownBy(() -> service.extrairClaims(token))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void rejeitaTokenAssinadoComOutraChave() {
        JwtService emissor = new JwtService(SECRET, 3600000);
        JwtService verificador = new JwtService("outra-chave-diferente-com-tamanho-suficiente-para-hs256", 3600000);

        String token = emissor.gerarToken(new Usuario("admin", "hash", Role.ADMIN));

        assertThatThrownBy(() -> verificador.extrairClaims(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void rejeitaSecretMenorQue256BitsNoStartup() {
        assertThatThrownBy(() -> new JwtService("curta", 3600000))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32 caracteres");
    }
}
