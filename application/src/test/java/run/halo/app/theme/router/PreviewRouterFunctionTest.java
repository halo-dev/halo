package run.halo.app.theme.router;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import run.halo.app.content.PostService;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.AnonymousUserConst;
import run.halo.app.theme.finders.PostPublicQueryService;
import run.halo.app.theme.finders.SinglePageConversionService;
import run.halo.app.theme.finders.vo.ContributorVo;
import run.halo.app.theme.finders.vo.PostVo;
import run.halo.app.theme.finders.vo.SinglePageVo;

/**
 * Tests for {@link PreviewRouterFunction}.
 *
 * @author guqing
 * @since 2.6.x
 */
@ExtendWith(SpringExtension.class)
class PreviewRouterFunctionTest {
    @Mock
    private ReactiveExtensionClient client;

    @Mock
    private PostPublicQueryService postPublicQueryService;

    @Mock
    private ViewNameResolver viewNameResolver;

    @Mock
    private ViewResolver viewResolver;

    @Mock
    private PostService postService;

    @Mock
    private SinglePageConversionService singlePageConversionService;

    @InjectMocks
    private PreviewRouterFunction previewRouterFunction;

    private WebTestClient webTestClient;

    @BeforeEach
    public void setUp() {
        webTestClient = WebTestClient.bindToRouterFunction(previewRouterFunction.previewRouter())
            .handlerStrategies(HandlerStrategies.builder()
                .viewResolver(viewResolver)
                .build())
            .build();

        when(viewResolver.resolveViewName(any(), any()))
            .thenReturn(Mono.just(new EmptyView() {
                @Override
                public Mono<Void> render(Map<String, ?> model, MediaType contentType,
                    ServerWebExchange exchange) {
                    return super.render(model, contentType, exchange);
                }
            }));
    }

    @Test
    @WithMockUser(username = "testuser")
    public void previewPost() {
        Post post = new Post();
        post.setMetadata(new Metadata());
        post.getMetadata().setName("post1");
        post.setSpec(new Post.PostSpec());
        post.getSpec().setOwner("testuser");
        post.getSpec().setHeadSnapshot("snapshot1");
        post.getSpec().setBaseSnapshot("snapshot2");
        post.getSpec().setTemplate("postTemplate");
        when(client.fetch(eq(Post.class), eq("post1"))).thenReturn(Mono.just(post));

        PostVo postVo = PostVo.from(post);
        postVo.setContributors(contributorVos());
        when(postPublicQueryService.convertToVo(eq(post), eq(post.getSpec().getHeadSnapshot())))
            .thenReturn(Mono.just(postVo));

        when(viewNameResolver.resolveViewNameOrDefault(any(), eq("postTemplate"),
            eq("post"))).thenReturn(Mono.just("postView"));

        webTestClient.get().uri("/preview/posts/post1")
            .exchange()
            .expectStatus().isOk();

        verify(viewResolver).resolveViewName(any(), any());
        verify(postPublicQueryService).convertToVo(eq(post), eq(post.getSpec().getHeadSnapshot()));
        verify(client).fetch(eq(Post.class), eq("post1"));
    }

    @Test
    public void previewPostWhenUnAuthenticated() {
        webTestClient.get().uri("/preview/posts/post1")
            .exchange()
            .expectStatus().isEqualTo(404);
    }

    @Test
    @WithMockUser(username = "testuser")
    public void previewSinglePage() {
        SinglePage singlePage = new SinglePage();
        singlePage.setMetadata(new Metadata());
        singlePage.getMetadata().setName("page1");
        singlePage.setSpec(new SinglePage.SinglePageSpec());
        singlePage.getSpec().setOwner("testuser");
        singlePage.getSpec().setHeadSnapshot("snapshot1");
        singlePage.getSpec().setTemplate("pageTemplate");
        when(client.fetch(SinglePage.class, "page1")).thenReturn(Mono.just(singlePage));

        SinglePageVo singlePageVo = SinglePageVo.from(singlePage);
        singlePageVo.setContributors(contributorVos());
        when(singlePageConversionService.convertToVo(singlePage, "snapshot1"))
            .thenReturn(Mono.just(singlePageVo));

        when(viewNameResolver.resolveViewNameOrDefault(any(), eq("pageTemplate"),
            eq("page"))).thenReturn(Mono.just("pageView"));

        webTestClient.get().uri("/preview/singlepages/page1")
            .exchange()
            .expectStatus().isOk();

        verify(viewResolver).resolveViewName(any(), any());
        verify(client).fetch(eq(SinglePage.class), eq("page1"));
    }

    @Test
    public void previewSinglePageWhenUnAuthenticated() {
        webTestClient.get().uri("/preview/singlepages/page1")
            .exchange()
            .expectStatus().isEqualTo(404);
    }

    @Test
    @WithMockUser(username = AnonymousUserConst.PRINCIPAL)
    public void previewWithAnonymousUser() {
        webTestClient.get().uri("/preview/singlepages/page1")
            .exchange()
            .expectStatus().isEqualTo(404);
    }

    List<ContributorVo> contributorVos() {
        ContributorVo contributorA = ContributorVo.builder()
            .name("fake-user")
            .build();
        ContributorVo contributorB = ContributorVo.builder()
            .name("testuser")
            .build();
        return List.of(contributorA, contributorB);
    }
}