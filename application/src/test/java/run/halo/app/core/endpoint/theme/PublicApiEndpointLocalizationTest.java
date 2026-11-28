package run.halo.app.core.endpoint.theme;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import run.halo.app.core.endpoint.console.CustomEndpointsBuilder;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.PageRequest;
import run.halo.app.localization.LocalizationContext;
import run.halo.app.localization.LocalizationScope;
import run.halo.app.localization.PublicApiLocalizationResolver;
import run.halo.app.localization.PublicApiLocalizationWebFilter;
import run.halo.app.localization.ResourceLocalizationProvider;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;
import run.halo.app.theme.finders.PostFinder;
import run.halo.app.theme.finders.PostPublicQueryService;
import run.halo.app.theme.finders.vo.ListedPostVo;

class PublicApiEndpointLocalizationTest {

    @Test
    void queriedLocaleReachesPostPublicApiSubscription() {
        var postFinder = mock(PostFinder.class);
        var publicQuery = mock(PostPublicQueryService.class);
        var extensions = mock(ExtensionGetter.class);
        var provider = mock(ResourceLocalizationProvider.class);
        var context = new LocalizationContext(
                Locale.SIMPLIFIED_CHINESE, Locale.ENGLISH, List.of(), LocalizationScope.PUBLIC_API);
        when(extensions.getEnabledExtension(ResourceLocalizationProvider.class)).thenReturn(Mono.just(provider));
        when(provider.resolveContext(Locale.ENGLISH, LocalizationScope.PUBLIC_API))
                .thenReturn(Mono.just(context));
        when(publicQuery.list(any(), any(PageRequest.class)))
                .thenReturn(LocalizationContext.current()
                        .filter(current -> current.equals(context))
                        .map(current -> new ListResult<ListedPostVo>(List.of()))
                        .switchIfEmpty(Mono.error(new AssertionError("Missing public API localization context"))));
        var endpoint = new PostQueryEndpoint(postFinder, publicQuery);
        var router = new CustomEndpointsBuilder().add(endpoint).build();
        var filter = new PublicApiLocalizationWebFilter(new PublicApiLocalizationResolver(extensions));

        WebTestClient.bindToRouterFunction(router)
                .webFilter(filter)
                .build()
                .get()
                .uri("/apis/api.content.halo.run/v1alpha1/posts?locale=en")
                .exchange()
                .expectStatus()
                .isOk();
    }
}
