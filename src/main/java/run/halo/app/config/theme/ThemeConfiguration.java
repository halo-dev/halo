package run.halo.app.config.theme;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.Nullable;
import org.springframework.ui.context.ThemeSource;
import org.springframework.ui.context.support.ResourceBundleThemeSource;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ThemeResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolverChain;
import run.halo.app.config.properties.HaloProperties;
import run.halo.app.service.OptionService;
import run.halo.app.service.ThemeService;
import run.halo.app.theme.ThemeManager;
import run.halo.app.utils.ThemeUtil;

/**
 * Theme and i18n configuration.
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

    @Bean
    LocaleResolver localeResolver() {
        return new CookieLocaleResolver();
    }

    @Bean
    ThemeSource themeSource(HaloProperties haloProperties, OptionService optionService,
        ApplicationContext context) {
        Locale defaultLocale = optionService.getLocale();
        return new I18nThemeSource(haloProperties.getThemesPrefix(), defaultLocale, context);
    }

    @Bean
    WebMvcConfigurer themingWebMvcConfigurer(HaloProperties haloProperties) {
        return new WebMvcConfigurer() {
            // add theming resource resolver which will be invoked by ResourceHttpRequestHandler
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry.addResourceHandler("/assets/**", "/source/**")
                    .addResourceLocations(haloProperties.getThemesPrefix())
                    // not support caching due to different cache key
                    .resourceChain(false)
                    .addResolver(new ThemingResourceResolver());
            }

            // support changing locale via parameter like {@code ?locale=en}
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(new LocaleChangeInterceptor());
            }
        };
    }

    /**
     * Get i18n resources under theme folder, e.g. {@code themeFolder/i18n/i18n_zh_CN.properties}.
     * <p>
     * priority: parameter -> cookie -> header -> default locale
     */
    private static class I18nThemeSource extends ResourceBundleThemeSource {
        private String prefix;
        private Locale defaultLocale;
        private ResourceLoader resourceLoader;
        private Map<String, ReloadableResourceBundleMessageSource> cache =
            new ConcurrentHashMap<>();

        I18nThemeSource(String prefix, Locale defaultLocale, ResourceLoader resourceLoader) {
            this.prefix = prefix.endsWith("/") ? prefix : prefix + "/";
            this.defaultLocale = defaultLocale;
            this.resourceLoader = resourceLoader;
        }

        /**
         * Replace the message resource to have the ability to load resource from physical files.
         *
         * @param basename just the theme name
         * @return i18n message source
         */
        protected MessageSource createMessageSource(String basename) {
            basename = prefix + basename + "/i18n/i18n";
            ReloadableResourceBundleMessageSource messageSource = cache.get(basename);
            if (messageSource == null) {
                synchronized (this) {
                    messageSource = cache.get(basename);
                    if (messageSource == null) {
                        messageSource = new ReloadableResourceBundleMessageSource();
                        messageSource.setBasename(basename);
                        messageSource.setDefaultEncoding("utf-8");
                        messageSource.setDefaultLocale(defaultLocale);
                        messageSource.setResourceLoader(this.resourceLoader);
                        cache.put(basename, messageSource);
                    }
                }
            }
            return messageSource;
        }

        /**
         * Invoke this method to reload properties.
         */
        public synchronized void clearCache() {
            cache.values().forEach(ReloadableResourceBundleMessageSource::clearCache);
        }
    }


    /**
     * Custom static resource resolver to support theming.
     */
    private static class ThemingResourceResolver extends PathResourceResolver {
        @Override
        public Resource resolveResource(@Nullable HttpServletRequest request, String requestPath,
            List<? extends Resource> locations, ResourceResolverChain chain) {
            Resource resource = null;
            // append theme prefix
            requestPath = "/" + ThemeUtil.getTheme(request) + request.getServletPath();

            // support responsive images
            // refer to https://medium.freecodecamp.org/a-guide-to-responsive-images-with-ready-to-use-templates-c400bd65c433
            String size = request.getParameter("size");
            if (StringUtils.hasText(size)) {
                String imagePath = imgUrl(requestPath, size);
                resource = super.resolveResource(request, imagePath, locations, chain);
            }

            // not image or fallback for responsive image
            if (resource == null) {
                resource = super.resolveResource(request, requestPath, locations, chain);
            }

            return resource;
        }

        public String imgUrl(String path, String size) {
            int i = path.lastIndexOf('.');
            if (i == -1) {
                return path + '-' + size;
            }
            String prefix = path.substring(0, i);
            String ext = path.substring(i);
            return prefix + '-' + size + ext;
        }
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

}
