package run.halo.app.theme.engine;

import java.nio.file.Path;
import java.util.Map;
import org.springframework.core.io.DefaultResourceLoader;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.spring6.templateresource.SpringResourceTemplateResource;
import org.thymeleaf.templateresolver.AbstractConfigurableTemplateResolver;
import org.thymeleaf.templateresource.FileTemplateResource;
import org.thymeleaf.templateresource.ITemplateResource;
import run.halo.app.theme.PageLayoutContract;

public class PageLayoutTemplateResolver extends AbstractConfigurableTemplateResolver {

    private final Path themePath;

    private final boolean themeLayoutSupported;

    private final DefaultResourceLoader resourceLoader;

    public PageLayoutTemplateResolver(Path themePath, boolean themeLayoutSupported) {
        this.themePath = themePath;
        this.themeLayoutSupported = themeLayoutSupported;
        this.resourceLoader = new DefaultResourceLoader(getClass().getClassLoader());
        setCacheable(false);
    }

    @Override
    protected ITemplateResource computeTemplateResource(
            IEngineConfiguration configuration,
            String ownerTemplate,
            String template,
            String resourceName,
            String characterEncoding,
            Map<String, Object> templateResolutionAttributes) {
        if (!PageLayoutContract.isPluginOwnedTemplate(ownerTemplate)
                || !PageLayoutContract.isContractTemplate(template)) {
            return null;
        }

        if (themeLayoutSupported) {
            var themeLayoutResourceName = computeResourceName(
                    configuration,
                    null,
                    PageLayoutContract.TEMPLATE_NAME,
                    themePath.resolve("templates") + "/",
                    getSuffix(),
                    getForceSuffix(),
                    getTemplateAliases(),
                    templateResolutionAttributes);
            return new FileTemplateResource(themeLayoutResourceName, characterEncoding);
        }

        var fallbackResourceName = computeResourceName(
                configuration,
                null,
                PageLayoutContract.TEMPLATE_NAME,
                getPrefix(),
                getSuffix(),
                getForceSuffix(),
                getTemplateAliases(),
                templateResolutionAttributes);
        return new SpringResourceTemplateResource(resourceLoader.getResource(fallbackResourceName), characterEncoding);
    }
}
