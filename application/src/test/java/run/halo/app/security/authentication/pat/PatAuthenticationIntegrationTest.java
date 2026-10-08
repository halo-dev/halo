package run.halo.app.security.authentication.pat;

import static org.assertj.core.api.Assertions.assertThat;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;
import run.halo.app.core.extension.User;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.security.PersonalAccessToken;
import run.halo.app.security.authentication.CryptoService;

@SpringBootTest
@AutoConfigureWebTestClient
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PatAuthenticationIntegrationTest {

    @Autowired
    WebTestClient webClient;

    @Autowired
    ReactiveExtensionClient client;

    @Autowired
    CryptoService cryptoService;

    private static final String USERNAME = "pat-user";

    private PersonalAccessToken pat;

    private String token;

    @BeforeEach
    void setUp() {
        var user = new User();
        user.setMetadata(new Metadata());
        user.getMetadata().setName(USERNAME);
        user.setSpec(new User.UserSpec());
        user.getSpec().setDisplayName("PAT user");
        user.getSpec().setEmail("pat-user@example.com");
        user.getSpec().setEmailVerified(true);
        client.create(user).block();

        var requestPat = new PersonalAccessToken();
        requestPat.getSpec().setName("Test PAT");
        requestPat.getSpec().setRoles(List.of("authenticated"));
        pat = webClient
                .post()
                .uri("/apis/uc.api.security.halo.run/v1alpha1/personalaccesstokens")
                .headers(headers -> headers.setBearerAuth(createJwt(USERNAME, "authenticated")))
                .bodyValue(requestPat)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(PersonalAccessToken.class)
                .returnResult()
                .getResponseBody();
        assertThat(pat).isNotNull();
        token = pat.getMetadata().getAnnotations().get("security.halo.run/access-token");
        assertThat(token).isNotBlank();
    }

    @Test
    void shouldRejectPatWhileUserIsDisabled() {
        getCurrentUser()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.user.metadata.name")
                .isEqualTo(USERNAME);

        updateUserStatus("disable");
        getCurrentUser().expectStatus().isUnauthorized();
        assertThat(client.get(PersonalAccessToken.class, pat.getMetadata().getName())
                        .block()
                        .getSpec()
                        .isRevoked())
                .isFalse();

        updateUserStatus("enable");
        getCurrentUser()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.user.metadata.name")
                .isEqualTo(USERNAME);
    }

    @Test
    void shouldRejectRevokedPatAfterUserIsEnabled() {
        updateUserStatus("disable");
        var storedPat = client.get(PersonalAccessToken.class, pat.getMetadata().getName())
                .block();
        storedPat.getSpec().setRevoked(true);
        client.update(storedPat).block();

        updateUserStatus("enable");
        getCurrentUser().expectStatus().isUnauthorized();
    }

    private WebTestClient.ResponseSpec getCurrentUser() {
        return webClient
                .get()
                .uri("/apis/api.console.halo.run/v1alpha1/users/-")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();
    }

    private void updateUserStatus(String action) {
        webClient
                .post()
                .uri("/apis/console.api.security.halo.run/v1alpha1/users/" + USERNAME + "/" + action)
                .headers(headers -> headers.setBearerAuth(createJwt("pat-admin", "super-role")))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.spec.disabled")
                .isEqualTo("disable".equals(action));
    }

    private String createJwt(String username, String role) {
        var encoder = new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(cryptoService.getJwk())));
        return encoder.encode(JwtEncoderParameters.from(
                        JwsHeader.with(SignatureAlgorithm.RS256)
                                .keyId(cryptoService.getJwk().getKeyID())
                                .build(),
                        JwtClaimsSet.builder()
                                .subject(username)
                                .claim("roles", List.of(role))
                                .build()))
                .getTokenValue();
    }
}
