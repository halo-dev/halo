package run.halo.app.theme.router.factories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import run.halo.app.content.TestPost;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.finders.PostFinder;
import run.halo.app.theme.finders.vo.PostVo;
import run.halo.app.theme.router.EmptyView;
import run.halo.app.theme.router.ViewNameResolver;

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
    private ViewNameResolver viewNameResolver;

    @Mock
    private ReactiveExtensionClient client;

    @InjectMocks
    private PostRouteFactory postRouteFactory;

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

        when(viewNameResolver.resolveViewNameOrDefault(any(), any(), any()))
            .thenReturn(Mono.just(DefaultTemplateEnum.POST.getValue()));

        RouterFunction<ServerResponse> routerFunction = postRouteFactory.create("/archives/{name}");
        WebTestClient webTestClient = getWebTestClient(routerFunction);

        when(viewResolver.resolveViewName(any(), any()))
            .thenReturn(Mono.just(new EmptyView() {
                @Override
                public Mono<Void> render(Map<String, ?> model, MediaType contentType,
                    ServerWebExchange exchange) {
                    assertThat(model).containsKey(ModelConst.TEMPLATE_ID);
                    assertThat(model.get(ModelConst.TEMPLATE_ID))
                        .isEqualTo(DefaultTemplateEnum.POST.getValue());
                    assertThat(model.get("name"))
                        .isEqualTo(post.getMetadata().getName());
                    assertThat(model.get("plural")).isEqualTo("posts");
                    assertThat(model.get("post")).isNotNull();
                    assertThat(model.get("groupVersionKind"))
                        .isEqualTo(GroupVersionKind.fromExtension(Post.class));
                    return super.render(model, contentType, exchange);
                }
            }));
        webTestClient.get()
            .uri("/archives/fake-name")
            .exchange()
            .expectStatus().isOk();
    }
}
