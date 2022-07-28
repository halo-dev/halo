package run.halo.app.theme;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.SimpleLocaleContext;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.i18n.LocaleContextResolver;

/**
 * @author guqing
 * @since 2.0.0
 */
public class ThemeLocaleContextResolver implements LocaleContextResolver {

    private static final String DEFAULT_PARAMETER_NAME = "language";

    private final List<Locale> supportedLocales = new ArrayList<>(4);

    @Nullable
    private Locale defaultLocale;


    /**
     * Configure supported locales to check against the requested locales
     * determined via {@link HttpHeaders#getAcceptLanguageAsLocales()}.
     *
     * @param locales the supported locales
     */
    public void setSupportedLocales(List<Locale> locales) {
        this.supportedLocales.clear();
        this.supportedLocales.addAll(locales);
    }

    /**
     * Return the configured list of supported locales.
     */
    public List<Locale> getSupportedLocales() {
        return this.supportedLocales;
    }

    /**
     * Configure a fixed default locale to fall back on if the request does not
     * have an "Accept-Language" header (not set by default).
     *
     * @param defaultLocale the default locale to use
     */
    public void setDefaultLocale(@Nullable Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    /**
     * The configured default locale, if any.
     * <p>This method may be overridden in subclasses.
     */
    @Nullable
    public Locale getDefaultLocale() {
        return this.defaultLocale;
    }

    @Override
    @NonNull
    public LocaleContext resolveLocaleContext(ServerWebExchange exchange) {
        List<Locale> requestLocales = null;
        try {
            List<String> languages = exchange.getRequest().getQueryParams()
                .get(DEFAULT_PARAMETER_NAME);
            if (languages != null) {
                requestLocales = languages.stream()
                    .map(Locale::new)
                    .toList();
            } else {
                requestLocales = exchange.getRequest().getHeaders().getAcceptLanguageAsLocales();
            }

        } catch (IllegalArgumentException ex) {
            // Invalid Accept-Language header: treat as empty for matching purposes
        }
        return new SimpleLocaleContext(resolveSupportedLocale(requestLocales));
    }

    @Nullable
    private Locale resolveSupportedLocale(@Nullable List<Locale> requestLocales) {
        if (CollectionUtils.isEmpty(requestLocales)) {
            return getDefaultLocale();  // may be null
        }
        List<Locale> supportedLocales = getSupportedLocales();
        if (supportedLocales.isEmpty()) {
            return requestLocales.get(0);  // never null
        }

        Locale languageMatch = null;
        for (Locale locale : requestLocales) {
            if (supportedLocales.contains(locale)) {
                if (languageMatch == null || languageMatch.getLanguage()
                    .equals(locale.getLanguage())) {
                    // Full match: language + country, possibly narrowed from earlier
                    // language-only match
                    return locale;
                }
            } else if (languageMatch == null) {
                // Let's try to find a language-only match as a fallback
                for (Locale candidate : supportedLocales) {
                    if (!StringUtils.hasLength(candidate.getCountry())
                        && candidate.getLanguage().equals(locale.getLanguage())) {
                        languageMatch = candidate;
                        break;
                    }
                }
            }
        }
        if (languageMatch != null) {
            return languageMatch;
        }

        Locale defaultLocale = getDefaultLocale();
        return (defaultLocale != null ? defaultLocale : requestLocales.get(0));
    }

    @Override
    public void setLocaleContext(@NonNull ServerWebExchange exchange,
        @Nullable LocaleContext locale) {
        throw new UnsupportedOperationException(
            "Cannot change HTTP accept header - use a different locale context resolution "
                + "strategy");
    }

}
