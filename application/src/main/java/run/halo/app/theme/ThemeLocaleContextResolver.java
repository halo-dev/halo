package run.halo.app.theme;

import java.util.Locale;
import java.util.TimeZone;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.SimpleTimeZoneAwareLocaleContext;
import org.springframework.http.HttpCookie;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
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
    public static final String TIME_ZONE_REQUEST_ATTRIBUTE_NAME =
        ThemeLocaleContextResolver.class.getName() + ".TIME_ZONE";
    public static final String LOCALE_REQUEST_ATTRIBUTE_NAME =
        ThemeLocaleContextResolver.class.getName() + ".LOCALE";

    public static final String DEFAULT_PARAMETER_NAME = "language";
    public static final String TIME_ZONE_COOKIE_NAME = "time_zone";

    private final Function<ServerWebExchange, TimeZone> defaultTimeZoneFunction =
        exchange -> getDefaultTimeZone();

    @Override
    @NonNull
    public LocaleContext resolveLocaleContext(@NonNull ServerWebExchange exchange) {
        parseLocaleCookieIfNecessary(exchange);

        Locale locale = getLocale(exchange);

        return new SimpleTimeZoneAwareLocaleContext(locale,
            exchange.getAttribute(TIME_ZONE_REQUEST_ATTRIBUTE_NAME));
    }

    @Nullable
    private Locale getLocale(ServerWebExchange exchange) {
        String language = exchange.getRequest().getQueryParams()
            .getFirst(DEFAULT_PARAMETER_NAME);

        Locale locale;
        if (StringUtils.isNotBlank(language)) {
            locale = new Locale(language);
        } else if (exchange.getAttribute(LOCALE_REQUEST_ATTRIBUTE_NAME) != null) {
            locale = exchange.getAttribute(LOCALE_REQUEST_ATTRIBUTE_NAME);
        } else {
            locale = super.resolveLocaleContext(exchange).getLocale();
        }
        return locale;
    }

    private TimeZone getDefaultTimeZone() {
        return TimeZone.getDefault();
    }

    private void parseLocaleCookieIfNecessary(ServerWebExchange exchange) {
        if (exchange.getAttribute(TIME_ZONE_REQUEST_ATTRIBUTE_NAME) == null) {
            TimeZone timeZone = null;
            HttpCookie cookie = exchange.getRequest()
                .getCookies()
                .getFirst(TIME_ZONE_COOKIE_NAME);
            if (cookie != null) {
                String value = cookie.getValue();
                timeZone = TimeZone.getTimeZone(value);
            }
            exchange.getAttributes().put(TIME_ZONE_REQUEST_ATTRIBUTE_NAME,
                (timeZone != null ? timeZone : this.defaultTimeZoneFunction.apply(exchange)));
        }

        if (exchange.getAttribute(LOCALE_REQUEST_ATTRIBUTE_NAME) == null) {
            HttpCookie cookie = exchange.getRequest()
                .getCookies()
                .getFirst(DEFAULT_PARAMETER_NAME);
            if (cookie != null) {
                String value = cookie.getValue();
                exchange.getAttributes()
                    .put(LOCALE_REQUEST_ATTRIBUTE_NAME, new Locale(value));
            }
        }
    }
}
