package run.halo.app.security.authentication.pat;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.web.server.authentication.ServerBearerTokenAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * PAT authentication converter.
 *
 * @author johnniang
 * @since 2.20.4
 */
class PatAuthenticationConverter extends ServerBearerTokenAuthenticationConverter {

    public static final String PAT_TOKEN_PREFIX = "pat_";

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        return super.convert(exchange)
            .cast(BearerTokenAuthenticationToken.class)
            .map(BearerTokenAuthenticationToken::getToken)
            .filter(token -> StringUtils.startsWith(token, PAT_TOKEN_PREFIX))
            .map(token -> StringUtils.removeStart(token, PAT_TOKEN_PREFIX))
            .map(BearerTokenAuthenticationToken::new);
    }
}
