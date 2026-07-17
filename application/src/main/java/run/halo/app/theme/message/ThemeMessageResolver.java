package run.halo.app.theme.message;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.core.io.ResourceLoader;
import org.thymeleaf.messageresolver.StandardMessageResolver;
import org.thymeleaf.spring6.templateresource.SpringResourceTemplateResource;
import org.thymeleaf.templateresource.FileTemplateResource;
import org.thymeleaf.templateresource.ITemplateResource;
import run.halo.app.theme.ThemeContext;

/**
 * @author guqing
 * @since 2.0.0
 */
public class ThemeMessageResolver extends StandardMessageResolver {

    private final ThemeContext theme;

    private final ResourceLoader resourceLoader;

    private final String templatePrefix;

    private final String templateSuffix;

    @Nullable
    private final String characterEncoding;

    private final Path themeTemplatesPath;

    public ThemeMessageResolver(
            ThemeContext theme,
            ResourceLoader resourceLoader,
            String templatePrefix,
            String templateSuffix,
            @Nullable String characterEncoding) {
        this.theme = theme;
        this.resourceLoader = resourceLoader;
        this.templatePrefix = templatePrefix;
        this.templateSuffix = templateSuffix;
        this.characterEncoding = characterEncoding;
        this.themeTemplatesPath =
                theme.getPath().resolve("templates").toAbsolutePath().normalize();
    }

    @Override
    protected Map<String, String> resolveMessagesForTemplate(
            String template, ITemplateResource templateResource, Locale locale) {
        var properties = new HashMap<String, String>();
        Optional.ofNullable(ThemeMessageResolutionUtils.resolveMessagesForTemplate(locale, theme))
                .ifPresent(properties::putAll);
        Optional.ofNullable(resolveHaloMessages(template, templateResource, locale))
                .ifPresent(properties::putAll);
        Optional.ofNullable(super.resolveMessagesForTemplate(template, templateResource, locale))
                .ifPresent(properties::putAll);
        return Collections.unmodifiableMap(properties);
    }

    @Nullable
    private Map<String, String> resolveHaloMessages(
            String template, ITemplateResource templateResource, Locale locale) {
        if (!(templateResource instanceof FileTemplateResource)) {
            return null;
        }
        var selectedTemplatePath =
                Path.of(templateResource.getDescription()).toAbsolutePath().normalize();
        if (!selectedTemplatePath.startsWith(themeTemplatesPath)) {
            return null;
        }

        var suffix = template.endsWith(templateSuffix) ? "" : templateSuffix;
        var resource = resourceLoader.getResource(templatePrefix + template + suffix);
        if (!resource.exists()) {
            return null;
        }
        var haloTemplateResource = new SpringResourceTemplateResource(resource, characterEncoding);
        return super.resolveMessagesForTemplate(template, haloTemplateResource, locale);
    }
}
