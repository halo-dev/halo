package run.halo.app.theme;

import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.SimpleLocaleContext;
import org.springframework.lang.NonNull;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.i18n.AcceptHeaderLocaleContextResolver;
import org.springframework.web.server.i18n.LocaleContextResolver;

/**
 * @author guqing
 * @since 2.0.0
 */
public class ThemeLocaleContextResolver extends AcceptHeaderLocaleContextResolver
    implements LocaleContextResolver {

    private static final String DEFAULT_PARAMETER_NAME = "language";


    @Override
    @NonNull
    public LocaleContext resolveLocaleContext(ServerWebExchange exchange) {
        String language = exchange.getRequest().getQueryParams()
            .getFirst(DEFAULT_PARAMETER_NAME);
        if (StringUtils.isNotBlank(language)) {
            return new SimpleLocaleContext(new Locale(language));
        } else {
            return super.resolveLocaleContext(exchange);
        }
    }
}
