package run.halo.app.theme;

import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.SimpleTimeZoneAwareLocaleContext;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;
import org.springframework.web.server.i18n.AcceptHeaderLocaleContextResolver;

/**
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
@Component(WebHttpHandlerBuilder.LOCALE_CONTEXT_RESOLVER_BEAN_NAME)
public class ThemeLocaleContextResolver extends AcceptHeaderLocaleContextResolver {

    public static final String LANGUAGE_PARAMETER_NAME = "language";

    public static final String LANGUAGE_COOKIE_NAME = LANGUAGE_PARAMETER_NAME;

    public static final String TIME_ZONE_COOKIE_NAME = "time_zone";

    @Override
    @NonNull
    public LocaleContext resolveLocaleContext(@NonNull ServerWebExchange exchange) {
        var request = exchange.getRequest();
        var locale = getLocaleFromQueryParameter(request)
            .or(() -> getLocaleFromCookie(request))
            .orElseGet(() -> super.resolveLocaleContext(exchange).getLocale());

        var timeZone = getTimeZoneFromCookie(request)
            .orElseGet(TimeZone::getDefault);

        return new SimpleTimeZoneAwareLocaleContext(locale, timeZone);
    }

    private Optional<Locale> getLocaleFromCookie(ServerHttpRequest request) {
        return Optional.ofNullable(request.getCookies().getFirst(LANGUAGE_COOKIE_NAME))
            .map(HttpCookie::getValue)
            .filter(StringUtils::isNotBlank)
            .map(Locale::forLanguageTag);
    }

    private Optional<Locale> getLocaleFromQueryParameter(ServerHttpRequest request) {
        return Optional.ofNullable(request.getQueryParams().getFirst(LANGUAGE_PARAMETER_NAME))
            .filter(StringUtils::isNotBlank)
            .map(Locale::forLanguageTag);
    }

    private Optional<TimeZone> getTimeZoneFromCookie(ServerHttpRequest request) {
        return Optional.ofNullable(request.getCookies().getFirst(TIME_ZONE_COOKIE_NAME))
            .map(HttpCookie::getValue)
            .filter(StringUtils::isNotBlank)
            .map(TimeZone::getTimeZone);
    }

}
