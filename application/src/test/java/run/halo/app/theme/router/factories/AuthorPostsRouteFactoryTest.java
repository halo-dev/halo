package run.halo.app.theme.router.factories;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.util.List;
import java.util.Locale;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RenderingResponse;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.User;
import run.halo.app.core.user.service.RoleService;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.localization.DefaultReactivePresentationExtensionClient;
import run.halo.app.localization.LocalizationContext;
import run.halo.app.localization.LocalizationScope;
import run.halo.app.localization.ResourcePresentationService;
import run.halo.app.localization.ResourceReference;
import run.halo.app.security.authorization.AuthorityUtils;
import run.halo.app.theme.finders.vo.UserVo;
import run.halo.app.theme.router.ModelConst;

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
    ResourcePresentationService presentation;

    @Mock
    RoleService roleService;

    @InjectMocks
    AuthorPostsRouteFactory authorPostsRouteFactory;

    @BeforeEach
    void wirePresentationClient() {
        ReflectionTestUtils.setField(
                authorPostsRouteFactory,
                "client",
                new DefaultReactivePresentationExtensionClient(client, presentation));
    }

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
        when(presentation.presentAll(List.of(source))).thenReturn(Mono.just(List.of(translated)));
        var route = spy(authorPostsRouteFactory);
        doReturn(Mono.just(true)).when(route).hasPostManageRole("author");
        var request = MockServerRequest.builder()
                .uri(URI.create("/authors/author"))
                .pathVariable("name", "author")
                .build();
        route.handlerFunction()
                .handle(request)
                .cast(RenderingResponse.class)
                .doOnNext(response -> Assertions.assertThat(response.model().get(ModelConst.RESOURCE_REFERENCE))
                        .isEqualTo(new ResourceReference(GroupVersionKind.fromExtension(User.class), "author")))
                .flatMap(response -> ((Mono<?>) response.model().get("author")).cast(UserVo.class))
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

    @Test
    void declaresOwnershipOnlyForExistingPublicAuthorRoutes() {
        when(client.countBy(eq(User.class), any())).thenReturn(Mono.just(1L));
        when(roleService.getRolesByUsername("author")).thenReturn(Flux.just("role"));
        when(roleService.contains(any(), eq(java.util.Set.of(AuthorityUtils.POST_CONTRIBUTOR_ROLE_NAME))))
                .thenReturn(Mono.just(true));

        StepVerifier.create(authorPostsRouteFactory.matches("/authors", "/authors/author", HttpMethod.GET))
                .expectNext(true)
                .verifyComplete();
        StepVerifier.create(authorPostsRouteFactory.matches("/authors", "/authors/author/page/2", HttpMethod.GET))
                .expectNext(true)
                .verifyComplete();
        StepVerifier.create(authorPostsRouteFactory.matches("/authors", "/authors/author/page/abc", HttpMethod.GET))
                .expectNext(false)
                .verifyComplete();
        StepVerifier.create(authorPostsRouteFactory.matches("/authors", "/authors/author", HttpMethod.POST))
                .expectNext(false)
                .verifyComplete();

        when(roleService.contains(any(), eq(java.util.Set.of(AuthorityUtils.POST_CONTRIBUTOR_ROLE_NAME))))
                .thenReturn(Mono.just(false));
        StepVerifier.create(authorPostsRouteFactory.matches("/authors", "/authors/author", HttpMethod.GET))
                .expectNext(false)
                .verifyComplete();

        when(client.countBy(eq(User.class), any())).thenReturn(Mono.just(0L));
        StepVerifier.create(authorPostsRouteFactory.matches("/authors", "/authors/missing", HttpMethod.GET))
                .expectNext(false)
                .verifyComplete();
    }
}
