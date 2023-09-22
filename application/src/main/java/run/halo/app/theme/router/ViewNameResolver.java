package run.halo.app.theme.router;

import java.nio.file.Files;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;
import run.halo.app.theme.ThemeResolver;

/**
 * The {@link ViewNameResolver} is used to resolve view name.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
@AllArgsConstructor
public class ViewNameResolver {
    private static final String TEMPLATES = "templates";
    private final ThemeResolver themeResolver;
    private final ThymeleafProperties thymeleafProperties;

    /**
     * Resolves view name.
     * If the {@param #name} cannot be resolved to the view, the {@param #defaultName} is returned.
     */
    public Mono<String> resolveViewNameOrDefault(ServerRequest request, String name,
        String defaultName) {
        if (StringUtils.isBlank(name)) {
            return Mono.justOrEmpty(defaultName);
        }
        return themeResolver.getTheme(request.exchange())
            .mapNotNull(themeContext -> {
                String templateResourceName = computeResourceName(name);
                var resourcePath = themeContext.getPath()
                    .resolve(TEMPLATES)
                    .resolve(templateResourceName);
                return Files.exists(resourcePath) ? name : defaultName;
            })
            .switchIfEmpty(Mono.justOrEmpty(defaultName));
    }

    String computeResourceName(String name) {
        Assert.notNull(name, "Name must not be null");
        return StringUtils.endsWith(name, thymeleafProperties.getSuffix())
            ? name : name + thymeleafProperties.getSuffix();
    }
}
