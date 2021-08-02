package run.halo.app.config.theme;

import org.springframework.boot.autoconfigure.freemarker.FreeMarkerProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.ui.freemarker.SpringTemplateLoader;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import run.halo.app.config.properties.HaloProperties;
import run.halo.app.utils.ThemeUtil;
import java.io.IOException;
import java.util.Properties;

/**
 * Freemarker theming configuration.
 *
 * @author Chuntung Ho
 */
@Configuration
public class FreemarkerConfiguration {

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
            String themeName = ThemeUtil.getTheme(null);
            Resource resource = resourceLoader.getResource(themesPath + themeName + "/" + name);
            return (resource.exists() ? resource : null);
        }
    }
}

