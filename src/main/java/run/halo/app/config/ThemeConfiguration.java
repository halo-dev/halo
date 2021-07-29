package run.halo.app.config;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerProperties;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.ui.freemarker.SpringTemplateLoader;
import org.springframework.util.MimeType;
import org.springframework.web.servlet.ThemeResolver;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.dialect.AbstractDialect;
import org.thymeleaf.dialect.IExpressionObjectDialect;
import org.thymeleaf.expression.IExpressionObjectFactory;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolution;
import run.halo.app.config.properties.HaloProperties;
import run.halo.app.service.ThemeService;
import run.halo.app.theme.ThemeManager;
import run.halo.app.theme.ThemeUtil;

/**
 * Theme configuration for freemarker and thymeleaf.
 *
 * @author Chuntung Ho
 */
@Configuration
public class ThemeConfiguration {
    @Bean
    ThemeManager themeManager(ViewResolver... viewResolvers) {
        return new ThemeManager(viewResolvers);
    }

    @Bean
    ThemeResolver themeResolver(ThemeService themeService) {
        return new ManagedThemeResolver(themeService);
    }

    /**
     * Use spring {@link ThemeResolver} to handle theming.
     */
    private static class ManagedThemeResolver implements ThemeResolver {
        private static final String THEME_REQUEST_ATTRIBUTE_NAME =
            ManagedThemeResolver.class.getName() + ".THEME";
        private ThemeService themeService;

        public ManagedThemeResolver(ThemeService themeService) {
            this.themeService = themeService;
        }

        @Override
        public String resolveThemeName(HttpServletRequest request) {
            String themeName = (String) request.getAttribute(THEME_REQUEST_ATTRIBUTE_NAME);
            if (themeName != null) {
                return themeName;
            }
            themeName = themeService.getActivatedTheme().getFolderName();
            request.setAttribute(THEME_REQUEST_ATTRIBUTE_NAME, themeName);
            return themeName;
        }

        @Override
        public void setThemeName(HttpServletRequest request, HttpServletResponse response,
            String themeName) {
            throw new UnsupportedOperationException("Can't set theme here");
        }
    }

    @Bean
    FreeMarkerConfigurer freeMarkerConfigurer(ApplicationContext context,
        FreeMarkerProperties properties, HaloProperties haloProperties) {
        FreeMarkerConfigurer configurer = new FreeMarkerConfigurer();

        // refer to org.springframework.boot.autoconfigure.freemarker
        // .AbstractFreeMarkerConfiguration.applyProperties
        configurer.setTemplateLoaderPaths(properties.getTemplateLoaderPath());
        configurer.setPreferFileSystemAccess(properties.isPreferFileSystemAccess());
        configurer.setDefaultEncoding(properties.getCharsetName());
        Properties settings = new Properties();
        settings.put("recognize_standard_file_extensions", "true");
        settings.putAll(properties.getSettings());
        configurer.setFreemarkerSettings(settings);

        // add theme template loader
        SpringTemplateLoader themingTemplateLoader =
            new ThemingTemplateLoader(context, haloProperties.getThemesPrefix());
        configurer.setPreTemplateLoaders(themingTemplateLoader);

        return configurer;
    }

    /**
     * Freemarker template loader that finds resource under current theme.
     */
    private static class ThemingTemplateLoader extends SpringTemplateLoader {
        private final ResourceLoader resourceLoader;
        private String themesPath;

        public ThemingTemplateLoader(ResourceLoader resourceLoader, String themesPath) {
            super(resourceLoader, themesPath);
            this.resourceLoader = resourceLoader;
            if (!themesPath.endsWith("/")) {
                themesPath += "/";
            }
            this.themesPath = themesPath;
        }

        public Object findTemplateSource(String name) throws IOException {
            String themeName = ThemeUtil.getTheme();
            Resource resource = resourceLoader.getResource(themesPath + themeName + "/" + name);
            return (resource.exists() ? resource : null);
        }
    }

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

    @Bean
    IExpressionObjectDialect thymeleafThemeDialect() {
        IExpressionObjectDialect dialect = new IExpressionObjectDialect() {
            @Override
            public String getName() {
                return "Theme Dialect";
            }

            @Override
            public IExpressionObjectFactory getExpressionObjectFactory() {
                return new ThemeExpressionObjectFactory();
            }
        };
        return dialect;
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
            String themeName = ThemeUtil.getTheme();

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
     *
     * <p>
     * refer to org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration
     * .ThymeleafWebMvcConfiguration.ThymeleafViewResolverConfiguration.thymeleafViewResolver
     */
    private static class VerifiedThymeleafViewResolver extends ThymeleafViewResolver {
        public VerifiedThymeleafViewResolver(SpringTemplateEngine templateEngine) {
            this.setTemplateEngine(templateEngine);
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

    /**
     * Thymeleaf expression for ThemeUtil.
     */
    static class ThemeExpressionObjectFactory implements IExpressionObjectFactory {
        private ThemeUtil themeUtil = new ThemeUtil();

        @Override
        public Set<String> getAllExpressionObjectNames() {
            return Collections.singleton("themeUtil");
        }

        @Override
        public Object buildObject(IExpressionContext context, String expressionObjectName) {
            return themeUtil;
        }

        @Override
        public boolean isCacheable(String expressionObjectName) {
            return true;
        }
    }

}
