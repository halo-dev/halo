package run.halo.app.security;

import static org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers;

import java.util.Collections;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.web.server.util.matcher.AndServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.MediaTypeServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

/**
 * The web session implementation of request cache for login parameter.
 *
 * @author johnniang
 * @since 2.24.0
 */
@Slf4j
@Component
class WebSessionLoginParameterRequestCache implements LoginParameterRequestCache {

    private static final String SESSION_ATTRIBUTE_NAME_PREFIX =
            WebSessionLoginParameterRequestCache.class + ".";

    private final ServerWebExchangeMatcher cacheMatcher = createDefaultRequestMatcher();

    @Override
    public Mono<Void> saveParameter(ServerWebExchange exchange, String parameterName) {
        return cacheMatcher
                .matches(exchange)
                .filter(ServerWebExchangeMatcher.MatchResult::isMatch)
                .flatMap(m -> loadParameter(exchange, parameterName))
                .delayUntil(
                        parameter ->
                                exchange.getSession()
                                        .map(WebSession::getAttributes)
                                        .doOnNext(
                                                attributes -> {
                                                    log.debug(
                                                            "Save {}: {} into session",
                                                            parameterName,
                                                            parameter);
                                                    attributes.put(
                                                            SESSION_ATTRIBUTE_NAME_PREFIX
                                                                    + parameterName,
                                                            parameter);
                                                }))
                .then();
    }

    @Override
    public Mono<Void> removeParameter(ServerWebExchange exchange, String parameterName) {
        return cacheMatcher
                .matches(exchange)
                .filter(ServerWebExchangeMatcher.MatchResult::isMatch)
                .flatMap(m -> exchange.getSession())
                .map(WebSession::getAttributes)
                .doOnNext(
                        attributes -> {
                            var removed =
                                    attributes.remove(
                                            SESSION_ATTRIBUTE_NAME_PREFIX + parameterName);
                            log.debug("Remove {}: {} from session", parameterName, removed);
                        })
                .then();
    }

    @Override
    public Mono<String> getParameter(ServerWebExchange exchange, String parameterName) {
        return exchange.getSession()
                .map(WebSession::getAttributes)
                .mapNotNull(
                        attributes -> {
                            var parameter =
                                    attributes.get(SESSION_ATTRIBUTE_NAME_PREFIX + parameterName);
                            log.debug("Get {}: {} from session", parameterName, parameter);
                            return parameter;
                        })
                .filter(String.class::isInstance)
                .cast(String.class);
    }

    private static Mono<String> loadParameter(ServerWebExchange exchange, String parameterName) {
        // load from query
        return Mono.justOrEmpty(exchange.getRequest().getQueryParams().getFirst(parameterName))
                .filter(Predicate.not(String::isBlank))
                .switchIfEmpty(
                        Mono.defer(
                                () ->
                                        exchange.getFormData()
                                                .mapNotNull(form -> form.getFirst(parameterName))
                                                .filter(Predicate.not(String::isBlank))));
    }

    private static ServerWebExchangeMatcher createDefaultRequestMatcher() {
        var post = pathMatchers(HttpMethod.POST, "/login/**", "/social/**", "/challenges/**");
        var form = new MediaTypeServerWebExchangeMatcher(MediaType.TEXT_HTML);
        form.setIgnoredMediaTypes(Collections.singleton(MediaType.ALL));
        return new AndServerWebExchangeMatcher(post, form);
    }
}
