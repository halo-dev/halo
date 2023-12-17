package run.halo.app.theme;

import java.nio.file.Files;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * The {@link DefaultViewNameResolver} is used to resolve view name.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
@AllArgsConstructor
public class DefaultViewNameResolver implements ViewNameResolver {
    private static final String TEMPLATES = "templates";
    private final ThemeResolver themeResolver;
    private final ThymeleafProperties thymeleafProperties;

    /**
     * Resolves view name.
     * If the {@param #name} cannot be resolved to the view, the {@param #defaultName} is returned.
     */
    @Override
    public Mono<String> resolveViewNameOrDefault(ServerWebExchange exchange, String name,
        String defaultName) {
        if (StringUtils.isBlank(name)) {
            return Mono.justOrEmpty(defaultName);
        }
        return themeResolver.getTheme(exchange)
            .mapNotNull(themeContext -> {
                String templateResourceName = computeResourceName(name);
                var resourcePath = themeContext.getPath()
                    .resolve(TEMPLATES)
                    .resolve(templateResourceName);
                return Files.exists(resourcePath) ? name : defaultName;
            })
            .switchIfEmpty(Mono.justOrEmpty(defaultName));
    }

    @Override
    public Mono<String> resolveViewNameOrDefault(ServerRequest request, String name,
        String defaultName) {
        return resolveViewNameOrDefault(request.exchange(), name, defaultName);
    }

    String computeResourceName(String name) {
        Assert.notNull(name, "Name must not be null");
        return StringUtils.endsWith(name, thymeleafProperties.getSuffix())
            ? name : name + thymeleafProperties.getSuffix();
    }
}
