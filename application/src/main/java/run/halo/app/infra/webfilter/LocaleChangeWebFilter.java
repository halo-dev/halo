package run.halo.app.infra.webfilter;

import static run.halo.app.theme.ThemeLocaleContextResolver.LANGUAGE_COOKIE_NAME;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
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
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import run.halo.app.theme.ThemeLocaleContextResolver;
import run.halo.app.theme.UserLocaleRequestAttributeWriteFilter;

/**
 * {@link UserLocaleRequestAttributeWriteFilter} is before {@link LocaleChangeWebFilter} to
 * obtain the locale.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class LocaleChangeWebFilter implements WebFilter {

    private final ServerWebExchangeMatcher matcher;
    private final ThemeLocaleContextResolver themeLocaleContextResolver;

    public LocaleChangeWebFilter(ThemeLocaleContextResolver themeLocaleContextResolver) {
        this.themeLocaleContextResolver = themeLocaleContextResolver;
        var pathMatcher = ServerWebExchangeMatchers.pathMatchers(HttpMethod.GET, "/**");
        var textHtmlMatcher = new MediaTypeServerWebExchangeMatcher(MediaType.TEXT_HTML);
        textHtmlMatcher.setIgnoredMediaTypes(Set.of(MediaType.ALL));
        matcher = new AndServerWebExchangeMatcher(pathMatcher, textHtmlMatcher);
    }

    @Override
    @NonNull
    public Mono<Void> filter(ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        return matcher.matches(exchange)
            .filter(MatchResult::isMatch)
            .doOnNext(result -> {
                var localeContext = themeLocaleContextResolver.resolveLocaleContext(exchange);
                var locale = localeContext.getLocale();
                if (locale != null) {
                    setLanguageCookieIfAbsent(exchange, locale);
                }
            })
            .then(Mono.defer(() -> chain.filter(exchange)));
    }

    void setLanguageCookieIfAbsent(ServerWebExchange exchange, Locale locale) {
        var languageCookie = exchange.getRequest().getCookies().getFirst(LANGUAGE_COOKIE_NAME);
        if (languageCookie != null
            && Objects.equals(languageCookie.getValue(), locale.toLanguageTag())) {
            return;
        }
        var cookie = ResponseCookie.from(LANGUAGE_COOKIE_NAME, locale.toLanguageTag())
            .path("/")
            .secure("https".equalsIgnoreCase(exchange.getRequest().getURI().getScheme()))
            .sameSite("Lax")
            .build();
        exchange.getResponse().getCookies().set(LANGUAGE_COOKIE_NAME, cookie);
    }
}
