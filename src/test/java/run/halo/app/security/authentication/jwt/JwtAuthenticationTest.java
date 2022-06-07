package run.halo.app.security.authentication.jwt;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import run.halo.app.security.LoginUtils;

@SpringBootTest
@AutoConfigureWebTestClient
class JwtAuthenticationTest {

    @Autowired
    WebTestClient webClient;

    @MockBean
    ReactiveUserDetailsService userDetailsService;

    @Test
    void accessProtectedApiWithoutToken() {
        webClient.get().uri("/api/v1/test/hello").exchange().expectStatus().isUnauthorized();
    }

    @Test
    void accessProtectedApiUsingBearerToken() {
        when(userDetailsService.findByUsername(anyString())).thenReturn(Mono.just(
            User.withDefaultPasswordEncoder()
                .username("username")
                .password("password")
                .roles("USER")
                .build()
        ));
        final var token = LoginUtils.login(webClient, "username", "password").block();
        webClient.get().uri("/api/v1/test/hello")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .exchange()
            .expectStatus().isForbidden()
            .expectHeader().value(HttpHeaders.WWW_AUTHENTICATE,
                containsString("insufficient_scope"));
    }

}
