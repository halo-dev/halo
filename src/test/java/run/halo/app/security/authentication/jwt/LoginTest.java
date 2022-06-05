package run.halo.app.security.authentication.jwt;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.nimbusds.jwt.JWTClaimNames;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest
@AutoConfigureWebTestClient
class LoginTest {

    @Autowired
    WebTestClient webClient;

    @Test
    void logintWithoutLoginRequest() {
        webClient.post().uri("/api/auth/token").exchange().expectStatus().isUnauthorized();
    }

    @Test
    void loginWithoutPostMethod() {
        webClient.get().uri("/api/auth/token").exchange().expectStatus().isUnauthorized();
        webClient.put().uri("/api/auth/token").exchange().expectStatus().isUnauthorized();
        webClient.delete().uri("/api/auth/token").exchange().expectStatus().isUnauthorized();
        webClient.patch().uri("/api/auth/token").exchange().expectStatus().isUnauthorized();
    }

    @Test
    void loginWithoutApplicationJsonAcceptHeader() {
        var request = new LoginAuthenticationConverter.UsernamePasswordRequest();
        request.setUsername("user");
        request.setPassword("invalid_password");
        webClient.post().uri("/api/auth/token")
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_VALUE).bodyValue(request)
            .exchange().expectStatus().isUnauthorized();
    }

    @Test
    void loginWithInvalidCredential() {
        var request = new LoginAuthenticationConverter.UsernamePasswordRequest();
        request.setUsername("user");
        request.setPassword("invalid_password");
        webClient.post().uri("/api/auth/token")
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE).bodyValue(request)
            .exchange().expectStatus().isBadRequest().expectHeader()
            .contentType(MediaType.APPLICATION_JSON).expectBody()
            .jsonPath("$.error").value(equalTo("Invalid Credentials"));
    }

    @Test
    void loginWithValidCredential(@Autowired ReactiveJwtDecoder jwtDecoder) {
        var request = new LoginAuthenticationConverter.UsernamePasswordRequest();
        request.setUsername("user");
        request.setPassword("password");
        webClient.post().uri("/api/auth/token")
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE).bodyValue(request)
            .exchange().expectStatus().isOk().expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody().jsonPath("$.token").value(token -> {
                var jwt = jwtDecoder.decode(token).block();
                assertNotNull(jwt);
                assertEquals("user", jwt.getClaim(JWTClaimNames.SUBJECT));
            }, String.class);
    }
}
