package run.halo.app.config.theme;

import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.util.MimeType;
import org.springframework.web.servlet.View;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolution;
import run.halo.app.config.properties.HaloProperties;
import run.halo.app.utils.ThemeUtil;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Thymeleaf theming configuration.
 *
 * @author Chuntung Ho
 */
@Configuration
public class ThymeleafConfiguration {

    @Bean("thymeleafViewResolver")
    ThymeleafViewResolver thymeleafViewResolver(ThymeleafProperties properties,
        SpringTemplateEngine templateEngine) {
        VerifiedThymeleafViewResolver resolver = new VerifiedThymeleafViewResolver(templateEngine);
        resolver.applyProperties(properties);
        return resolver;
    }

    @Bean("themingTemplateResolver")
    SpringResourceTemplateResolver themingTemplateResolver(ApplicationContext context,
        ThymeleafProperties properties, HaloProperties haloProperties) {
        ThemingTemplateResolver resolver =
            new ThemingTemplateResolver(haloProperties.getThemesPrefix());
        resolver.setApplicationContext(context);
        resolver.applyProperties(properties);
        return resolver;
    }

    /**
     * Extra thymeleaf template resolver that computes resource name with current theme.
     */
    private static class ThemingTemplateResolver extends SpringResourceTemplateResolver {
        public ThemingTemplateResolver(String themesPrefix) {
            if (!themesPrefix.endsWith("/")) {
                themesPrefix += "/";
            }
            setPrefix(themesPrefix);
        }

        @Override
        protected String computeResourceName(
            final IEngineConfiguration configuration, final String ownerTemplate,
            final String template,
            final String prefix, final String suffix, final boolean forceSuffix,
            final Map<String, String> templateAliases,
            final Map<String, Object> templateResolutionAttributes) {
            String themeName = ThemeUtil.getTheme(null);

            return super
                .computeResourceName(configuration, ownerTemplate, template,
                    prefix + themeName + "/", suffix, forceSuffix, templateAliases,
                    templateResolutionAttributes);
        }

        // refer to org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration
        // .DefaultTemplateResolverConfiguration.defaultTemplateResolver
        public void applyProperties(ThymeleafProperties properties) {
            setSuffix(properties.getSuffix());
            setTemplateMode(properties.getMode());
            if (properties.getEncoding() != null) {
                setCharacterEncoding(properties.getEncoding().name());
            }
            setCacheable(properties.isCache());
            setCheckExistence(properties.isCheckTemplate());
            // order is required for multi resolver, prior to default resolver
            setOrder(properties.getTemplateResolverOrder() - 1);
        }
    }

    /**
     * Custom thymeleaf view resolver that checks template existing before resolving view.
     * <br>
     * Since view cache will be clear after switching theme, caching by theme is not needed.
     * <p>
     * refer to org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration
     * .ThymeleafWebMvcConfiguration.ThymeleafViewResolverConfiguration.thymeleafViewResolver
     */
    private static class VerifiedThymeleafViewResolver extends ThymeleafViewResolver {
        public VerifiedThymeleafViewResolver(SpringTemplateEngine templateEngine) {
            this.setTemplateEngine(templateEngine);
        }

        /**
         * Invoke this method to reload templates after updating theme files.
         */
        public void clearTemplateCache() {
            ((SpringTemplateEngine) this.getTemplateEngine()).clearTemplateCache();
        }

        @Override
        protected View loadView(final String viewName, final Locale locale) throws Exception {
            // no considering bean view
            boolean existing = false;
            for (ITemplateResolver templateResolver : this.getTemplateEngine()
                .getConfiguration().getTemplateResolvers()) {
                TemplateResolution resolution = templateResolver
                    .resolveTemplate(this.getTemplateEngine().getConfiguration(), null,
                        viewName, null);
                if (resolution != null && resolution.getTemplateResource().exists()) {
                    existing = true;
                    break;
                }
            }

            if (existing) {
                return super.loadView(viewName, locale);
            } else {
                return null;
            }
        }

        // refer to org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration
        // .ThymeleafWebMvcConfiguration.ThymeleafViewResolverConfiguration.thymeleafViewResolver
        public void applyProperties(ThymeleafProperties properties) {
            setCharacterEncoding(properties.getEncoding().name());
            setContentType(appendCharset(properties.getServlet().getContentType(),
                getCharacterEncoding()));
            setProducePartialOutputWhileProcessing(
                properties.getServlet().isProducePartialOutputWhileProcessing());
            setExcludedViewNames(properties.getExcludedViewNames());
            setViewNames(properties.getViewNames());
            setCache(properties.isCache());
            setOrder(Ordered.LOWEST_PRECEDENCE - 5);
        }

        private String appendCharset(MimeType type, String charset) {
            if (type.getCharset() != null) {
                return type.toString();
            }
            LinkedHashMap<String, String> parameters = new LinkedHashMap<>();
            parameters.put("charset", charset);
            parameters.putAll(type.getParameters());
            return new MimeType(type, parameters).toString();
        }
    }
}