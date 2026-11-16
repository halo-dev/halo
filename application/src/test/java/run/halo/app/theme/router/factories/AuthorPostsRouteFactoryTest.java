package run.halo.app.theme.router.factories;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Locale;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.User;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.localization.LocalizationContext;
import run.halo.app.localization.LocalizationScope;
import run.halo.app.localization.ResourceLocalizationService;

/**
 * Tests for {@link AuthorPostsRouteFactory}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class AuthorPostsRouteFactoryTest extends RouteFactoryTestSuite {
    @Mock
    ReactiveExtensionClient client;

    @Mock
    ResourceLocalizationService localizationService;

    @InjectMocks
    AuthorPostsRouteFactory authorPostsRouteFactory;

    @Test
    void create() {
        var spyAuthorRoute = spy(authorPostsRouteFactory);
        doReturn(Mono.just(true)).when(spyAuthorRoute).hasPostManageRole(anyString());

        RouterFunction<ServerResponse> routerFunction = spyAuthorRoute.create(null);
        WebTestClient webClient = getWebTestClient(routerFunction);

        when(client.fetch(eq(User.class), eq("fake-user"))).thenReturn(Mono.just(new User()));
        webClient.get().uri("/authors/fake-user").exchange().expectStatus().isOk();

        webClient
                .get()
                .uri("/authors/fake-user/page/p")
                .exchange()
                .expectStatus()
                .isNotFound();

        webClient
                .get()
                .uri("/authors/fake-user/page/abc")
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void projectsAuthorTemplateVariableAndKeepsPasswordProtected() {
        var source = new User();
        var metadata = new Metadata();
        metadata.setName("author");
        source.setMetadata(metadata);
        source.setSpec(new User.UserSpec());
        source.getSpec().setDisplayName("原作者");
        source.getSpec().setPassword("password-hash");
        var translated = new User();
        translated.setMetadata(metadata);
        translated.setSpec(new User.UserSpec());
        translated.getSpec().setDisplayName("Author");
        translated.getSpec().setPassword("password-hash");
        var context = new LocalizationContext(Locale.CHINA, Locale.ENGLISH, List.of(), LocalizationScope.PUBLIC_PAGE);
        when(client.fetch(User.class, "author")).thenReturn(Mono.just(source));
        when(localizationService.localize(source, context)).thenReturn(Mono.just(translated));
        authorPostsRouteFactory
                .getByName("author")
                .contextWrite(ctx -> ctx.put(LocalizationContext.class, context))
                .as(StepVerifier::create)
                .assertNext(vo -> {
                    Assertions.assertThat(vo.getSpec().getDisplayName()).isEqualTo("Author");
                    Assertions.assertThat(vo.getSpec().getPassword()).isEqualTo("[PROTECTED]");
                    Assertions.assertThat(vo.getMetadata().getName()).isEqualTo("author");
                })
                .verifyComplete();
        Assertions.assertThat(source.getSpec().getDisplayName()).isEqualTo("原作者");
    }
}
