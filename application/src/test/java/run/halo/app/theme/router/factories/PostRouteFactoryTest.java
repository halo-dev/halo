package run.halo.app.theme.router.factories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.i18n.SimpleLocaleContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.i18n.LocaleContextResolver;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.content.TestPost;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.ReactivePresentationExtensionClient;
import run.halo.app.infra.ExternalUrlSupplier;
import run.halo.app.localization.ResourceReference;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.finders.PostFinder;
import run.halo.app.theme.finders.vo.PostVo;
import run.halo.app.theme.router.*;

/**
 * Tests for {@link PostRouteFactory}.
 *
 * @author guqing
 * @since 2.3.0
 */
@ExtendWith(MockitoExtension.class)
class PostRouteFactoryTest extends RouteFactoryTestSuite {

    @Mock
    private PostFinder postFinder;

    @Mock
    private PostViewNameResolver postViewNameResolver;

    @Mock
    private ReactivePresentationExtensionClient client;

    @Mock
    private ReactiveQueryPostPredicateResolver predicateResolver;

    @Mock
    private LocaleContextResolver localeContextResolver;

    @Mock
    private TitleVisibilityIdentifyCalculator titleVisibilityIdentifyCalculator;

    @Mock
    private ExternalUrlSupplier externalUrl;

    @InjectMocks
    private PostRouteFactory postRouteFactory;

    @Test
    void shouldBeSameResultWhenParsePattenMultiply() {
        var parser = new PostRouteFactory.PatternParser("/?p={slug}");
        Assertions.assertTrue(parser.isQueryParamPattern());
        parser = new PostRouteFactory.PatternParser("/?p={slug}");
        Assertions.assertTrue(parser.isQueryParamPattern());
    }

    @Test
    void create() {
        Post post = TestPost.postV1();
        Map<String, String> labels = MetadataUtil.nullSafeLabels(post);
        labels.put(Post.PUBLISHED_LABEL, "true");
        post.getMetadata().setName("fake-name");
        post.getSpec().setDeleted(false);
        post.getSpec().setVisible(Post.VisibleEnum.PUBLIC);
        when(postFinder.getByName(eq("fake-name"))).thenReturn(Mono.just(PostVo.from(post)));

        when(client.fetch(eq(Post.class), eq("fake-name"))).thenReturn(Mono.just(post));

        when(postViewNameResolver.resolveViewNameOrDefault(any(ServerRequest.class), any(PostVo.class)))
                .thenReturn(Mono.just(DefaultTemplateEnum.POST.getValue()));
        when(predicateResolver.getPredicate()).thenReturn(new DefaultQueryPostPredicateResolver().getPredicate());

        RouterFunction<ServerResponse> routerFunction = postRouteFactory.create("/archives/{name}");
        WebTestClient webTestClient = getWebTestClient(routerFunction);

        when(localeContextResolver.resolveLocaleContext(any()))
                .thenReturn(new SimpleLocaleContext(Locale.getDefault()));
        when(viewResolver.resolveViewName(any(), any())).thenReturn(Mono.just(new EmptyView() {
            @Override
            public Mono<Void> render(Map<String, ?> model, MediaType contentType, ServerWebExchange exchange) {
                assertThat(model).containsKey(ModelConst.TEMPLATE_ID);
                assertThat(model.get(ModelConst.TEMPLATE_ID)).isEqualTo(DefaultTemplateEnum.POST.getValue());
                assertThat(model.get("name")).isEqualTo(post.getMetadata().getName());
                assertThat(model.get("plural")).isEqualTo("posts");
                assertThat(model.get("post")).isNotNull();
                assertThat(model.get("groupVersionKind")).isEqualTo(GroupVersionKind.fromExtension(Post.class));
                assertThat(model.get(ModelConst.RESOURCE_REFERENCE))
                        .isEqualTo(new ResourceReference(GroupVersionKind.fromExtension(Post.class), "fake-name"));
                return super.render(model, contentType, exchange);
            }
        }));
        webTestClient.get().uri("/archives/fake-name").exchange().expectStatus().isOk();
    }

    @Test
    void describesPublishedPostOwnership() {
        when(externalUrl.get()).thenReturn(URI.create("https://example.com/blog"));
        var post = publishedPost("fake-name", "hello", "https://example.com/blog/news/hello");
        when(client.listAll(eq(Post.class), any(), any())).thenReturn(Flux.just(post));

        StepVerifier.create(postRouteFactory.claimedPrefixes("/{category}/{slug}"))
                .expectNext(Set.of("news"))
                .verifyComplete();
        StepVerifier.create(postRouteFactory.matches("/{category}/{slug}", "/news/hello", HttpMethod.GET))
                .expectNext(true)
                .verifyComplete();
        StepVerifier.create(postRouteFactory.matches("/{slug}", "/en", HttpMethod.GET))
                .expectNext(false)
                .verifyComplete();
    }

    private static Post publishedPost(String name, String slug, String permalink) {
        var metadata = new Metadata();
        metadata.setName(name);
        metadata.setLabels(Map.of(Post.PUBLISHED_LABEL, "true"));
        var post = new Post();
        post.setMetadata(metadata);
        post.setSpec(new Post.PostSpec());
        post.getSpec().setSlug(slug);
        post.getSpec().setDeleted(false);
        post.setStatus(new Post.PostStatus());
        post.getStatus().setPermalink(permalink);
        return post;
    }
}
