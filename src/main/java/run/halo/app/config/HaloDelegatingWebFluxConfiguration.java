package run.halo.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.config.DelegatingWebFluxConfiguration;
import org.springframework.web.server.i18n.LocaleContextResolver;
import run.halo.app.theme.ThemeLocaleContextResolver;

/**
 * @author guqing
 * @since 2.0.0
 */
@Configuration(proxyBeanMethods = false)
public class HaloDelegatingWebFluxConfiguration extends DelegatingWebFluxConfiguration {

    @Override
    @NonNull
    protected LocaleContextResolver createLocaleContextResolver() {
        return new ThemeLocaleContextResolver();
    }
}
