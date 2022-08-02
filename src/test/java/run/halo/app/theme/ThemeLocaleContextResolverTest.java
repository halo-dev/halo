package run.halo.app.theme;

import static java.util.Locale.CANADA;
import static java.util.Locale.CHINA;
import static java.util.Locale.CHINESE;
import static java.util.Locale.ENGLISH;
import static java.util.Locale.GERMAN;
import static java.util.Locale.GERMANY;
import static java.util.Locale.JAPAN;
import static java.util.Locale.JAPANESE;
import static java.util.Locale.KOREA;
import static java.util.Locale.UK;
import static java.util.Locale.US;
import static org.assertj.core.api.Assertions.assertThat;
import static run.halo.app.theme.ThemeLocaleContextResolver.DEFAULT_PARAMETER_NAME;
import static run.halo.app.theme.ThemeLocaleContextResolver.TIME_ZONE_COOKIE_NAME;

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.TimeZone;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.TimeZoneAwareLocaleContext;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;

/**
 * Test for {@link ThemeLocaleContextResolver}.
 *
 * @author guqing
 * @since 2.0.0
 */
class ThemeLocaleContextResolverTest {
    private final ThemeLocaleContextResolver resolver = new ThemeLocaleContextResolver();

    @Test
    public void resolveTimeZone() {
        TimeZoneAwareLocaleContext localeContext =
            (TimeZoneAwareLocaleContext) this.resolver.resolveLocaleContext(
                exchangeTimeZone(CHINA));
        assertThat(localeContext.getTimeZone()).isNotNull();
        assertThat(localeContext.getTimeZone())
            .isEqualTo(TimeZone.getTimeZone("America/Adak"));
        assertThat(localeContext.getLocale()).isNotNull();
        assertThat(localeContext.getLocale().getLanguage()).isEqualTo("en");
    }

    @Test
    public void resolve() {
        assertThat(this.resolver.resolveLocaleContext(exchange(CANADA)).getLocale())
            .isEqualTo(CANADA);
        assertThat(this.resolver.resolveLocaleContext(exchange(US, CANADA)).getLocale())
            .isEqualTo(US);
    }

    @Test
    public void resolveFromParam() {
        assertThat(this.resolver.resolveLocaleContext(exchangeForParam("en")).getLocale())
            .isEqualTo(ENGLISH);
        assertThat(this.resolver.resolveLocaleContext(exchangeForParam("zh")).getLocale())
            .isEqualTo(CHINESE);
    }

    @Test
    public void resolvePreferredSupported() {
        this.resolver.setSupportedLocales(Collections.singletonList(CANADA));
        assertThat(this.resolver.resolveLocaleContext(exchange(US, CANADA)).getLocale()).isEqualTo(
            CANADA);
    }

    @Test
    public void resolvePreferredNotSupported() {
        this.resolver.setSupportedLocales(Collections.singletonList(CANADA));
        assertThat(this.resolver.resolveLocaleContext(exchange(US, UK)).getLocale()).isEqualTo(US);
    }

    @Test
    public void resolvePreferredNotSupportedWithDefault() {
        this.resolver.setSupportedLocales(Arrays.asList(US, JAPAN));
        this.resolver.setDefaultLocale(JAPAN);
        assertThat(this.resolver.resolveLocaleContext(exchange(KOREA)).getLocale()).isEqualTo(
            JAPAN);
    }

    @Test
    public void resolvePreferredAgainstLanguageOnly() {
        this.resolver.setSupportedLocales(Collections.singletonList(ENGLISH));
        assertThat(
            this.resolver.resolveLocaleContext(exchange(GERMANY, US, UK)).getLocale()).isEqualTo(
            ENGLISH);
    }

    @Test
    public void resolvePreferredAgainstCountryIfPossible() {
        this.resolver.setSupportedLocales(Arrays.asList(ENGLISH, UK));
        assertThat(
            this.resolver.resolveLocaleContext(exchange(GERMANY, US, UK)).getLocale()).isEqualTo(
            UK);
    }

    @Test
    public void resolvePreferredAgainstLanguageWithMultipleSupportedLocales() {
        this.resolver.setSupportedLocales(Arrays.asList(GERMAN, US));
        assertThat(
            this.resolver.resolveLocaleContext(exchange(GERMANY, US, UK)).getLocale()).isEqualTo(
            GERMAN);
    }

    @Test
    public void resolveMissingAcceptLanguageHeader() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        assertThat(this.resolver.resolveLocaleContext(exchange).getLocale()).isNull();
    }

    @Test
    public void resolveMissingAcceptLanguageHeaderWithDefault() {
        this.resolver.setDefaultLocale(US);

        MockServerHttpRequest request = MockServerHttpRequest.get("/").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        assertThat(this.resolver.resolveLocaleContext(exchange).getLocale()).isEqualTo(US);
    }

    @Test
    public void resolveEmptyAcceptLanguageHeader() {
        MockServerHttpRequest request =
            MockServerHttpRequest.get("/").header(HttpHeaders.ACCEPT_LANGUAGE, "").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        assertThat(this.resolver.resolveLocaleContext(exchange).getLocale()).isNull();
    }

    @Test
    public void resolveEmptyAcceptLanguageHeaderWithDefault() {
        this.resolver.setDefaultLocale(US);

        MockServerHttpRequest request =
            MockServerHttpRequest.get("/").header(HttpHeaders.ACCEPT_LANGUAGE, "").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        assertThat(this.resolver.resolveLocaleContext(exchange).getLocale()).isEqualTo(US);
    }

    @Test
    public void resolveInvalidAcceptLanguageHeader() {
        MockServerHttpRequest request =
            MockServerHttpRequest.get("/").header(HttpHeaders.ACCEPT_LANGUAGE, "en_US").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        assertThat(this.resolver.resolveLocaleContext(exchange).getLocale()).isNull();
    }

    @Test
    public void resolveInvalidAcceptLanguageHeaderWithDefault() {
        this.resolver.setDefaultLocale(US);

        MockServerHttpRequest request =
            MockServerHttpRequest.get("/").header(HttpHeaders.ACCEPT_LANGUAGE, "en_US").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        assertThat(this.resolver.resolveLocaleContext(exchange).getLocale()).isEqualTo(US);
    }

    @Test
    public void defaultLocale() {
        this.resolver.setDefaultLocale(JAPANESE);
        MockServerHttpRequest request = MockServerHttpRequest.get("/").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        assertThat(this.resolver.resolveLocaleContext(exchange).getLocale()).isEqualTo(JAPANESE);

        request = MockServerHttpRequest.get("/").acceptLanguageAsLocales(US).build();
        exchange = MockServerWebExchange.from(request);
        assertThat(this.resolver.resolveLocaleContext(exchange).getLocale()).isEqualTo(US);
    }


    private ServerWebExchange exchange(Locale... locales) {
        return MockServerWebExchange.from(
            MockServerHttpRequest.get("").acceptLanguageAsLocales(locales));
    }

    private ServerWebExchange exchangeTimeZone(Locale... locales) {
        return MockServerWebExchange.from(
            MockServerHttpRequest.get("").acceptLanguageAsLocales(locales)
                .cookie(new HttpCookie(TIME_ZONE_COOKIE_NAME, "America/Adak"))
                .cookie(new HttpCookie(DEFAULT_PARAMETER_NAME, "en")));
    }

    private ServerWebExchange exchangeForParam(String language) {
        return MockServerWebExchange.from(
            MockServerHttpRequest.get("/index?language=" + language));
    }
}
