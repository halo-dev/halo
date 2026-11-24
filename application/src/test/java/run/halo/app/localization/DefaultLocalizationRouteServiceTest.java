package run.halo.app.localization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;

class DefaultLocalizationRouteServiceTest {
    private final ExtensionGetter extensionGetter = mock(ExtensionGetter.class);
    private final DefaultLocalizationRouteService service = new DefaultLocalizationRouteService(extensionGetter);

    @Test
    void mergesPrefixesAndMatchesAnyEnabledProvider() {
        var first = new Provider(Set.of("posts"), "/posts/hello", true);
        var second = new Provider(Set.of("gallery", "shop"), "/gallery/one", true);
        when(extensionGetter.getEnabledExtensions(LocalizationRouteProvider.class))
                .thenReturn(Flux.just(first, second));

        StepVerifier.create(service.claimedPrefixes())
                .assertNext(prefixes -> assertThat(prefixes).containsExactlyInAnyOrder("posts", "gallery", "shop"))
                .verifyComplete();
        StepVerifier.create(service.matches("/gallery/one", HttpMethod.GET))
                .expectNext(true)
                .verifyComplete();
        StepVerifier.create(service.matches("/unknown", HttpMethod.GET))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void emptyProviderSetOwnsNothing() {
        when(extensionGetter.getEnabledExtensions(LocalizationRouteProvider.class))
                .thenReturn(Flux.empty());

        StepVerifier.create(service.claimedPrefixes())
                .assertNext(prefixes -> assertThat(prefixes).isEmpty())
                .verifyComplete();
        StepVerifier.create(service.matches("/en", HttpMethod.GET))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void coreProvidersRemainActiveWhenConfigurationSelectsOnlyPluginProviders() {
        var core = new Provider(Set.of("archives"), "/archives", true);
        var plugin = new PluginProvider(Set.of("shop"), "/shop/one");
        when(extensionGetter.getEnabledExtensions(LocalizationRouteProvider.class))
                .thenReturn(Flux.just(plugin));
        var localizationRoutes = new DefaultLocalizationRouteService(extensionGetter, List.of(core));

        StepVerifier.create(localizationRoutes.claimedPrefixes())
                .assertNext(prefixes -> assertThat(prefixes).containsExactlyInAnyOrder("archives", "shop"))
                .verifyComplete();
        StepVerifier.create(localizationRoutes.matches("/archives", HttpMethod.GET))
                .expectNext(true)
                .verifyComplete();
        StepVerifier.create(localizationRoutes.matches("/shop/one", HttpMethod.GET))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void failingProviderDoesNotHideOtherPublicRoutes() {
        var healthy = new Provider(Set.of("posts"), "/posts/hello", true);
        var broken = new LocalizationRouteProvider() {
            @Override
            public Mono<Set<String>> claimedPrefixes() {
                throw new IllegalStateException("broken provider");
            }

            @Override
            public Mono<Boolean> matches(String path, HttpMethod method) {
                return Mono.error(new IllegalStateException("broken provider"));
            }
        };
        when(extensionGetter.getEnabledExtensions(LocalizationRouteProvider.class))
                .thenReturn(Flux.just(broken, healthy));

        StepVerifier.create(service.claimedPrefixes())
                .assertNext(prefixes -> assertThat(prefixes).containsExactly("posts"))
                .verifyComplete();
        StepVerifier.create(service.matches("/posts/hello", HttpMethod.GET))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void stalledProviderDoesNotBlockRouteQueries() {
        var stalled = new LocalizationRouteProvider() {
            @Override
            public Mono<Set<String>> claimedPrefixes() {
                return Mono.never();
            }

            @Override
            public Mono<Boolean> matches(String path, HttpMethod method) {
                return Mono.never();
            }
        };
        when(extensionGetter.getEnabledExtensions(LocalizationRouteProvider.class))
                .thenReturn(Flux.just(stalled));

        StepVerifier.withVirtualTime(() -> service.claimedPrefixes())
                .thenAwait(Duration.ofSeconds(3))
                .expectNext(Set.of())
                .verifyComplete();
        StepVerifier.withVirtualTime(() -> service.matches("/posts/hello", HttpMethod.GET))
                .thenAwait(Duration.ofSeconds(3))
                .expectNext(false)
                .verifyComplete();
    }

    private record Provider(Set<String> prefixes, String ownedPath, boolean owned)
            implements LocalizationRouteProvider {
        @Override
        public Mono<Set<String>> claimedPrefixes() {
            return Mono.just(prefixes);
        }

        @Override
        public Mono<Boolean> matches(String path, HttpMethod method) {
            return Mono.just(owned && path.equals(ownedPath));
        }
    }

    private record PluginProvider(Set<String> prefixes, String ownedPath) implements LocalizationRouteProvider {
        @Override
        public Mono<Set<String>> claimedPrefixes() {
            return Mono.just(prefixes);
        }

        @Override
        public Mono<Boolean> matches(String path, HttpMethod method) {
            return Mono.just(path.equals(ownedPath));
        }
    }
}
