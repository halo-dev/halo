package run.halo.app.localization;

import java.time.Duration;
import java.util.IllformedLocaleException;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;

/** Resolves an explicitly requested public API language into a localization context. */
@Component
@RequiredArgsConstructor
public class PublicApiLocalizationResolver {
    private final ExtensionGetter extensions;

    Mono<LocalizationContext> resolve(String languageTag) {
        return Mono.defer(() -> {
                    if (languageTag == null || languageTag.isBlank()) {
                        return Mono.empty();
                    }
                    Locale requested;
                    try {
                        requested =
                                new Locale.Builder().setLanguageTag(languageTag).build();
                    } catch (IllformedLocaleException ignored) {
                        return Mono.empty();
                    }
                    return extensions
                            .getEnabledExtension(ResourceLocalizationProvider.class)
                            .flatMap(provider -> provider.resolveContext(requested, LocalizationScope.PUBLIC_API))
                            .filter(context -> context.scope() == LocalizationScope.PUBLIC_API);
                })
                .timeout(Duration.ofSeconds(2))
                .onErrorResume(error -> Mono.empty());
    }
}
