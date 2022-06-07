package run.halo.app.security.authentication.jwt;

import static org.springframework.security.authentication.UsernamePasswordAuthenticationToken.unauthenticated;

import java.util.List;
import lombok.Data;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class LoginAuthenticationConverter implements ServerAuthenticationConverter {

    private final List<HttpMessageReader<?>> reader;

    public LoginAuthenticationConverter(List<HttpMessageReader<?>> reader) {
        this.reader = reader;
    }

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        return ServerRequest.create(exchange, this.reader)
            .bodyToMono(UsernamePasswordRequest.class)
            .map(request -> unauthenticated(request.getUsername(), request.getPassword()));
    }

    @Data
    public static class UsernamePasswordRequest {

        private String username;

        private String password;

    }
}
