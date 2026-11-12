package run.halo.app.theme;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.thymeleaf.autoconfigure.ThymeleafProperties;
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
     * Resolves view name. If the {@param #name} cannot be resolved to the view, the {@param #defaultName} is returned.
     */
    @Override
    public Mono<String> resolveViewNameOrDefault(ServerWebExchange exchange, String name, String defaultName) {
        if (StringUtils.isBlank(name) || !isSafeViewName(name)) {
            return Mono.justOrEmpty(defaultName);
        }
        return themeResolver
                .getTheme(exchange)
                .mapNotNull(themeContext -> resolveContainedViewName(themeContext, name, defaultName))
                .switchIfEmpty(Mono.justOrEmpty(defaultName));
    }

    @Override
    public Mono<String> resolveViewNameOrDefault(ServerRequest request, String name, String defaultName) {
        return resolveViewNameOrDefault(request.exchange(), name, defaultName);
    }

    String computeResourceName(String name) {
        Assert.notNull(name, "Name must not be null");
        return StringUtils.endsWith(name, thymeleafProperties.getSuffix())
                ? name
                : name + thymeleafProperties.getSuffix();
    }

    /**
     * Rejects names that can escape the theme templates directory or select Thymeleaf fragments.
     *
     * <p>Invalid names fall back to {@code defaultName} instead of throwing, because view resolution should remain a
     * soft lookup.
     */
    static boolean isSafeViewName(String name) {
        if (name.contains("..") || name.indexOf('\\') >= 0 || name.contains("::")) {
            return false;
        }
        try {
            return !Path.of(name).isAbsolute();
        } catch (InvalidPathException e) {
            return false;
        }
    }

    String resolveContainedViewName(ThemeContext themeContext, String name, String defaultName) {
        var templateResourceName = computeResourceName(name);
        try {
            var templatesBase =
                    themeContext.getPath().resolve(TEMPLATES).toAbsolutePath().normalize();
            var resourcePath =
                    templatesBase.resolve(templateResourceName).toAbsolutePath().normalize();
            if (!resourcePath.startsWith(templatesBase) || !Files.exists(resourcePath)) {
                return defaultName;
            }
            return name;
        } catch (InvalidPathException e) {
            return defaultName;
        }
    }
}
