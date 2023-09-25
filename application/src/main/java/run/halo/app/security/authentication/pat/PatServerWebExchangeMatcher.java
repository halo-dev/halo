package run.halo.app.security.authentication.pat;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.web.server.authentication.ServerBearerTokenAuthenticationConverter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class PatServerWebExchangeMatcher implements ServerWebExchangeMatcher {

    public static final String PAT_TOKEN_PREFIX = "pat_";

    private final ServerAuthenticationConverter authConverter =
        new ServerBearerTokenAuthenticationConverter();

    @Override
    public Mono<MatchResult> matches(ServerWebExchange exchange) {
        return authConverter.convert(exchange)
            .filter(a -> a instanceof BearerTokenAuthenticationToken)
            .cast(BearerTokenAuthenticationToken.class)
            .map(BearerTokenAuthenticationToken::getToken)
            .filter(tokenString -> StringUtils.startsWith(tokenString, PAT_TOKEN_PREFIX))
            .flatMap(t -> MatchResult.match())
            .onErrorResume(AuthenticationException.class, t -> MatchResult.notMatch())
            .switchIfEmpty(Mono.defer(MatchResult::notMatch));
    }
}
