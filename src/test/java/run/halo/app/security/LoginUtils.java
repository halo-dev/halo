package run.halo.app.security;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import run.halo.app.security.authentication.jwt.LoginAuthenticationConverter;

public final class LoginUtils {

    public static Mono<String> login(WebTestClient webClient, String username, String password) {
        var request = new LoginAuthenticationConverter.UsernamePasswordRequest();
        request.setUsername(username);
        request.setPassword(password);
        return webClient.post().uri("/api/auth/token")
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE).bodyValue(request)
            .exchange().expectStatus().isOk().expectHeader().contentType(MediaType.APPLICATION_JSON)
            .returnResult(ResponseMap.class)
            .getResponseBody()
            .single()
            .map(responseMap -> responseMap.get("token").toString());
    }
}
