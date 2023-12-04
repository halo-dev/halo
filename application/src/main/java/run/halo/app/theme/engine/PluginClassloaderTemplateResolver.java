package run.halo.app.theme.engine;

import static run.halo.app.plugin.PluginConst.SYSTEM_PLUGIN_NAME;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.pf4j.PluginState;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.lang.Nullable;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.spring6.templateresource.SpringResourceTemplateResource;
import org.thymeleaf.templateresolver.AbstractConfigurableTemplateResolver;
import org.thymeleaf.templateresource.ITemplateResource;
import run.halo.app.plugin.HaloPluginManager;

/**
 * Plugin classloader template resolver to resolve template by plugin classloader.
 *
 * @author guqing
 * @since 2.11.0
 */
public class PluginClassloaderTemplateResolver extends AbstractConfigurableTemplateResolver {

    private final HaloPluginManager haloPluginManager;
    static final Pattern PLUGIN_TEMPLATE_PATTERN =
        Pattern.compile("plugin:([A-Za-z0-9\\-.]+):(.+)");

    /**
     * Create a new plugin classloader template resolver, not cacheable.
     *
     * @param haloPluginManager plugin manager must not be null
     */
    public PluginClassloaderTemplateResolver(HaloPluginManager haloPluginManager) {
        super();
        this.haloPluginManager = haloPluginManager;
        setCacheable(false);
    }

    @Override
    protected ITemplateResource computeTemplateResource(
        final IEngineConfiguration configuration, final String ownerTemplate, final String template,
        final String resourceName, final String characterEncoding,
        final Map<String, Object> templateResolutionAttributes) {
        var matchResult = matchPluginTemplate(ownerTemplate, template);
        if (!matchResult.matches()) {
            return null;
        }
        String pluginName = matchResult.pluginName();
        var classloader = getClassloaderByPlugin(pluginName);
        if (classloader == null) {
            return null;
        }

        var templateName = matchResult.templateName();
        var ownerTemplateName = matchResult.ownerTemplateName();

        String handledResourceName = computeResourceName(configuration, ownerTemplateName,
            templateName, getPrefix(), getSuffix(), getForceSuffix(), getTemplateAliases(),
            templateResolutionAttributes);

        var resource = new DefaultResourceLoader(classloader)
            .getResource(handledResourceName);
        return new SpringResourceTemplateResource(resource, characterEncoding);
    }

    MatchResult matchPluginTemplate(String ownerTemplate, String template) {
        boolean matches = false;
        String pluginName = null;
        String templateName = template;
        String ownerTemplateName = ownerTemplate;
        if (StringUtils.isNotBlank(ownerTemplate)) {
            Matcher ownerTemplateMatcher = PLUGIN_TEMPLATE_PATTERN.matcher(ownerTemplate);
            if (ownerTemplateMatcher.matches()) {
                matches = true;
                pluginName = ownerTemplateMatcher.group(1);
                ownerTemplateName = ownerTemplateMatcher.group(2);
            }
        }
        Matcher templateMatcher = PLUGIN_TEMPLATE_PATTERN.matcher(template);
        if (templateMatcher.matches()) {
            matches = true;
            pluginName = templateMatcher.group(1);
            templateName = templateMatcher.group(2);
        }
        return new MatchResult(pluginName, ownerTemplateName, templateName, matches);
    }

    record MatchResult(String pluginName, String ownerTemplateName, String templateName,
                       boolean matches) {
    }

    @Nullable
    private ClassLoader getClassloaderByPlugin(String pluginName) {
        if (SYSTEM_PLUGIN_NAME.equals(pluginName)) {
            return this.getClass().getClassLoader();
        }
        var pluginWrapper = haloPluginManager.getPlugin(pluginName);
        if (pluginWrapper == null || !PluginState.STARTED.equals(pluginWrapper.getPluginState())) {
            return null;
        }
        return pluginWrapper.getPluginClassLoader();
    }
}
