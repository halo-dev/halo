package run.halo.app.theme;

import java.util.Locale;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.SystemSetting;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class UserLocaleRequestAttributeWriteFilter implements WebFilter {
    public static final String USER_LOCALE_ATTRIBUTE =
        UserLocaleRequestAttributeWriteFilter.class.getName() + ".USER_LOCALE_ATTRIBUTE";

    private final SystemConfigurableEnvironmentFetcher environmentFetcher;

    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        return environmentFetcher.getBasic()
            .map(SystemSetting.Basic::useSystemLocale)
            .doOnNext(localeOpt -> localeOpt
                .ifPresent(locale -> exchange.getAttributes().put(USER_LOCALE_ATTRIBUTE, locale))
            )
            .then(chain.filter(exchange));
    }

    public static Optional<Locale> getUserLocale(ServerHttpRequest request) {
        return Optional.ofNullable((Locale) request.getAttributes()
            .get(USER_LOCALE_ATTRIBUTE));
    }
}
