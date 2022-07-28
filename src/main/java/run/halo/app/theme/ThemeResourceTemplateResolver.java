package run.halo.app.theme;

import java.nio.file.Path;
import java.util.Map;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.templateresolver.AbstractConfigurableTemplateResolver;
import org.thymeleaf.templateresource.FileTemplateResource;
import org.thymeleaf.templateresource.ITemplateResource;
import run.halo.app.infra.NotFoundException;
import run.halo.app.infra.utils.FilePathUtils;

/**
 * <p>Theme resource template resolver.</p>
 * <p>Search for files in the corresponding themes directory according to the current
 * theme name.
 * If the template file does not exist, a {@link NotFoundException} exception is thrown.</p>
 *
 * @author guqing
 * @since 2.0.0
 */
public class ThemeResourceTemplateResolver extends AbstractConfigurableTemplateResolver {
    private static final String THEME_SOURCE_LOCATION = "templates";

    @Override
    protected ITemplateResource computeTemplateResource(
        final IEngineConfiguration configuration, final String ownerTemplate,
        final String template, final String resourceName, final String characterEncoding,
        final Map<String, Object> templateResolutionAttributes) {
        ThemeContext themeContext = ThemeContextHolder.getThemeContext();
        Path path = FilePathUtils.combinePath(themeContext.getPath().toString(),
            THEME_SOURCE_LOCATION, resourceName);
        FileTemplateResource fileTemplateResource =
            new FileTemplateResource(path.toFile(), characterEncoding);
        if (!fileTemplateResource.exists()) {
            throw new NotFoundException(
                String.format("Template [%s] not found in theme [%s]", resourceName,
                    themeContext.getThemeName()));
        }
        return fileTemplateResource;
    }

}