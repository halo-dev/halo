package run.halo.app.theme.router;

import java.util.Locale;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;
import run.halo.app.theme.HaloViewResolver;

/**
 * The {@link ViewNameResolver} is used to resolve view name.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
@AllArgsConstructor
public class ViewNameResolver {
    private final HaloViewResolver haloViewResolver;
    private final ThymeleafProperties thymeleafProperties;

    /**
     * Resolves view name.
     * If the {@param #name} cannot be resolved to the view, the {@param #defaultName} is returned.
     */
    public Mono<String> resolveViewNameOrDefault(ServerRequest request, String name,
        String defaultName) {
        if (StringUtils.isBlank(name)) {
            return Mono.just(defaultName);
        }
        final String nameToUse = processName(name);
        Locale locale = LocaleContextHolder.getLocale(request.exchange().getLocaleContext());
        return haloViewResolver.resolveViewName(nameToUse, locale)
            .map(view -> nameToUse)
            .switchIfEmpty(Mono.just(defaultName));
    }

    String processName(String name) {
        String nameToLookup = name;
        if (StringUtils.endsWith(name, thymeleafProperties.getSuffix())) {
            nameToLookup = StringUtils.substringBeforeLast(name, thymeleafProperties.getSuffix());
        }
        return nameToLookup;
    }
}
