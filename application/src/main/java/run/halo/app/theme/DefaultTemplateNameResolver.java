package run.halo.app.theme;

import static run.halo.app.plugin.PluginConst.SYSTEM_PLUGIN_NAME;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import run.halo.app.plugin.PluginApplicationContext;

/**
 * A default implementation of {@link TemplateNameResolver}, It will be provided for plugins to
 * resolve template name.
 *
 * @author guqing
 * @since 2.11.0
 */
public class DefaultTemplateNameResolver implements TemplateNameResolver {

    private final ApplicationContext applicationContext;
    private final ViewNameResolver viewNameResolver;

    public DefaultTemplateNameResolver(ViewNameResolver viewNameResolver,
        ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.viewNameResolver = viewNameResolver;
    }

    @Override
    public Mono<String> resolveTemplateNameOrDefault(ServerWebExchange exchange, String name) {
        if (applicationContext instanceof PluginApplicationContext pluginApplicationContext) {
            var pluginName = pluginApplicationContext.getPluginId();
            return this.resolveTemplateNameOrDefault(exchange, name,
                pluginClassPathTemplate(pluginName, name));
        }
        return resolveTemplateNameOrDefault(exchange, name,
            pluginClassPathTemplate(SYSTEM_PLUGIN_NAME, name));
    }

    @Override
    public Mono<String> resolveTemplateNameOrDefault(ServerWebExchange exchange, String name,
        String defaultName) {
        return viewNameResolver.resolveViewNameOrDefault(exchange, name, defaultName);
    }

    @Override
    public Mono<Boolean> isTemplateAvailableInTheme(ServerWebExchange exchange, String name) {
        return this.resolveTemplateNameOrDefault(exchange, name, "")
            .filter(StringUtils::isNotBlank)
            .hasElement();
    }

    String pluginClassPathTemplate(String pluginName, String templateName) {
        return "plugin:" + pluginName + ":" + templateName;
    }
}
