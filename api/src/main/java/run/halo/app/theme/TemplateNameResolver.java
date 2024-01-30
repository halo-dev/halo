package run.halo.app.theme;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * <p>The {@link TemplateNameResolver} is used to resolve template name.</p>
 * <code>Halo</code> has a theme mechanism, template files are provided by different themes, so
 * we need a method to determine whether the template file exists in the activated theme and if
 * it does not exist, provide a default template name.
 *
 * @author guqing
 * @since 2.11.0
 */
public interface TemplateNameResolver {

    /**
     * Resolve template name if exists or default template name in classpath.
     *
     * @param exchange exchange to resolve theme to use
     * @param name template
     * @return template name if exists or default template name in classpath
     */
    Mono<String> resolveTemplateNameOrDefault(ServerWebExchange exchange, String name);

    /**
     * Resolve template name if exists or default template given.
     *
     * @param exchange exchange to resolve theme to use
     * @param name template name
     * @param defaultName default template name to use if given template name not exists
     * @return template name if exists or default template name given
     */
    Mono<String> resolveTemplateNameOrDefault(ServerWebExchange exchange, String name,
        String defaultName);

    /**
     * Determine whether the template file exists in the current theme.
     *
     * @param exchange exchange to resolve theme to use
     * @param name template name
     * @return <code>true</code> if the template file exists in the current theme, false otherwise
     */
    Mono<Boolean> isTemplateAvailableInTheme(ServerWebExchange exchange, String name);
}
