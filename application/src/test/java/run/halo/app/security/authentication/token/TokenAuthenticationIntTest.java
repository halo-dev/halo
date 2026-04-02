package run.halo.app.security.authentication.token;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;
import run.halo.app.security.authentication.CryptoService;

@SpringBootTest
@AutoConfigureWebTestClient
class TokenAuthenticationIntTest {

    @Autowired
    WebTestClient webClient;

    @Autowired
    CryptoService cryptoService;

    @Test
    void shouldUnauthorizeWithoutJwt() {
        webClient.get()
            .uri("/api/fake")
            .exchange()
            .expectStatus().is3xxRedirection()
            .expectHeader().location("/login?authentication_required");
    }

    @Test
    void shouldForbiddenWithJwtAndInsufficientRole() {
        var jwt = createJwt("fake-user", List.of());
        webClient.get()
            .uri("/api/fake")
            .headers(headers -> headers.setBearerAuth(jwt.getTokenValue()))
            .exchange()
            // Expect forbidden here due to insufficient role
            .expectStatus().isForbidden();
    }

    @Test
    void shouldUnauthorizeWithPat() {
        var jwt = createPat();
        webClient.get()
            .uri("/api/fake")
            .headers(headers -> headers.setBearerAuth(jwt.getTokenValue()))
            .exchange()
            // Expect unauthorized here due to PAT not being accepted for this endpoint
            .expectStatus().isUnauthorized();
    }

    @Test
    void shouldAuthenticateWithJwtAndSufficientRole() {
        var jwt = createJwt("anonymousUser", List.of("authenticated"));
        webClient.get()
            .uri("/apis/api.console.halo.run/v1alpha1/users/-")
            .headers(headers -> headers.setBearerAuth(jwt.getTokenValue()))
            .exchangeSuccessfully()
            .expectBody()
            .jsonPath("$.user.metadata.name").isEqualTo(jwt.getSubject());
    }

    Jwt createJwt(String username, List<String> roles) {
        var jwtEncoder =
            new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(cryptoService.getJwk())));
        return jwtEncoder.encode(JwtEncoderParameters.from(
            JwsHeader.with(SignatureAlgorithm.RS256)
                .keyId(cryptoService.getJwk().getKeyID())
                .build(),
            JwtClaimsSet.builder()
                .subject(username)
                .claim("roles", roles)
                .build()
        ));
    }

    Jwt createPat() {
        var jwtEncoder =
            new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(cryptoService.getJwk())));
        return jwtEncoder.encode(JwtEncoderParameters.from(
            JwsHeader.with(SignatureAlgorithm.RS256)
                .keyId(cryptoService.getJwk().getKeyID())
                .build(),
            JwtClaimsSet.builder()
                .subject("fake-user")
                .claim("pat_name", "fake-pat-name")
                .build()
        ));
    }
}
