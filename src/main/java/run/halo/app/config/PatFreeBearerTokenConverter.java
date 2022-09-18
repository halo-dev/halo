package run.halo.app.config;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.web.server.ServerBearerTokenAuthenticationConverter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class PatFreeBearerTokenConverter implements ServerAuthenticationConverter {

    private final ServerAuthenticationConverter delegate =
        new ServerBearerTokenAuthenticationConverter();

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        return Mono.fromCallable(
                () -> {
                    var authorization =
                        exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                    if (StringUtils.startsWithIgnoreCase(authorization, "bearer pat_")) {
                        // ignore PAT
                        return null;
                    }
                    return exchange;
                })
            .flatMap(delegate::convert);
    }
}
