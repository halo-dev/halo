package run.halo.app.infra.webfilter;

import static run.halo.app.theme.ThemeLocaleContextResolver.LANGUAGE_COOKIE_NAME;
import static run.halo.app.theme.ThemeLocaleContextResolver.LANGUAGE_PARAMETER_NAME;

import java.util.Locale;
import java.util.Set;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.lang.NonNull;
import org.springframework.security.web.server.util.matcher.AndServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.MediaTypeServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher.MatchResult;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class LocaleChangeWebFilter implements WebFilter {

    private final ServerWebExchangeMatcher matcher;

    public LocaleChangeWebFilter() {
        var pathMatcher = ServerWebExchangeMatchers.pathMatchers(HttpMethod.GET, "/**");
        var textHtmlMatcher = new MediaTypeServerWebExchangeMatcher(MediaType.TEXT_HTML);
        textHtmlMatcher.setIgnoredMediaTypes(Set.of(MediaType.ALL));
        matcher = new AndServerWebExchangeMatcher(pathMatcher, textHtmlMatcher);
    }

    @Override
    @NonNull
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        var request = exchange.getRequest();
        return matcher.matches(exchange)
            .filter(MatchResult::isMatch)
            .doOnNext(result -> {
                var language = request
                    .getQueryParams()
                    .getFirst(LANGUAGE_PARAMETER_NAME);
                if (StringUtils.hasText(language)) {
                    var locale = Locale.forLanguageTag(language);
                    setLanguageCookie(exchange, locale);
                }
            })
            .then(Mono.defer(() -> chain.filter(exchange)));
    }

    void setLanguageCookie(ServerWebExchange exchange, Locale locale) {
        var cookie = ResponseCookie.from(LANGUAGE_COOKIE_NAME, locale.toLanguageTag())
            .path("/")
            .secure("https".equalsIgnoreCase(exchange.getRequest().getURI().getScheme()))
            .sameSite("Lax")
            .build();
        exchange.getResponse().getCookies().set(LANGUAGE_COOKIE_NAME, cookie);
    }
}
