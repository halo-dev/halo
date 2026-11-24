package run.halo.app.localization;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;

/** Default aggregation of public route declarations from Core and enabled plugins. */
@Slf4j
@Component
public class DefaultLocalizationRouteService implements LocalizationRouteService {
    private static final Duration PROVIDER_TIMEOUT = Duration.ofSeconds(2);
    private final ExtensionGetter extensionGetter;
    private final List<LocalizationRouteProvider> coreProviders;

    @Autowired
    public DefaultLocalizationRouteService(
            ExtensionGetter extensionGetter, List<LocalizationRouteProvider> coreProviders) {
        this.extensionGetter = extensionGetter;
        this.coreProviders = List.copyOf(coreProviders);
    }

    DefaultLocalizationRouteService(ExtensionGetter extensionGetter) {
        this(extensionGetter, List.of());
    }

    @Override
    public Mono<Set<String>> claimedPrefixes() {
        return providers()
                .concatMap(provider -> safe(provider, "claimedPrefixes", provider::claimedPrefixes, Set.of())
                        .defaultIfEmpty(Set.of()))
                .reduce(new HashSet<String>(), (prefixes, claimed) -> {
                    prefixes.addAll(claimed);
                    return prefixes;
                })
                .map(Set::copyOf)
                .defaultIfEmpty(Set.of());
    }

    @Override
    public Mono<Boolean> matches(String path, HttpMethod method) {
        return providers()
                .concatMap(provider -> safe(provider, "matches", () -> provider.matches(path, method), false)
                        .defaultIfEmpty(false))
                .any(Boolean.TRUE::equals)
                .defaultIfEmpty(false);
    }

    private static <T> Mono<T> safe(
            LocalizationRouteProvider provider, String operation, Supplier<Mono<T>> call, T fallback) {
        return Mono.defer(call).timeout(PROVIDER_TIMEOUT).onErrorResume(error -> {
            log.warn(
                    "Ignoring failed {} from localization route provider {}",
                    operation,
                    provider.getClass().getName(),
                    error);
            return Mono.just(fallback);
        });
    }

    private Flux<LocalizationRouteProvider> providers() {
        var coreTypes = coreProviders.stream()
                .map(provider -> provider.getClass().getName())
                .collect(java.util.stream.Collectors.toSet());
        return Flux.concat(
                Flux.fromIterable(coreProviders),
                extensionGetter
                        .getEnabledExtensions(LocalizationRouteProvider.class)
                        .filter(provider ->
                                !coreTypes.contains(provider.getClass().getName())));
    }
}
