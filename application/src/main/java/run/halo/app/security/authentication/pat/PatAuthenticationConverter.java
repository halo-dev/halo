package run.halo.app.security.authentication.pat;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.web.server.authentication.ServerBearerTokenAuthenticationConverter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class PatAuthenticationConverter implements ServerAuthenticationConverter {

    private final ServerBearerTokenAuthenticationConverter bearerTokenConverter;

    public PatAuthenticationConverter() {
        bearerTokenConverter = new ServerBearerTokenAuthenticationConverter();
    }

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        return bearerTokenConverter.convert(exchange)
            .filter(token -> token instanceof BearerTokenAuthenticationToken)
            .cast(BearerTokenAuthenticationToken.class)
            .filter(this::isPersonalAccessToken)
            .cast(Authentication.class);
    }

    private boolean isPersonalAccessToken(BearerTokenAuthenticationToken bearerToken) {
        String token = bearerToken.getToken();
        return token.startsWith("pat_");
    }
}
