package run.halo.app.theme.router.factories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.content.Tag;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.PageRequest;
import run.halo.app.extension.ReactivePresentationExtensionClient;
import run.halo.app.localization.ResourceReference;
import run.halo.app.theme.finders.PostFinder;
import run.halo.app.theme.finders.TagFinder;
import run.halo.app.theme.finders.vo.TagVo;
import run.halo.app.theme.router.EmptyView;
import run.halo.app.theme.router.ModelConst;

/**
 * Tests for @link TagPostRouteFactory}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class TagPostRouteFactoryTest extends RouteFactoryTestSuite {
    @Mock
    private ReactivePresentationExtensionClient client;

    @Mock
    private TagFinder tagFinder;

    @Mock
    private PostFinder postFinder;

    @InjectMocks
    TagPostRouteFactory tagPostRouteFactory;

    @Test
    void create() {
        when(client.listBy(eq(Tag.class), any(), any(PageRequest.class)))
                .thenReturn(Mono.just(ListResult.emptyResult()));
        WebTestClient webTestClient = getWebTestClient(tagPostRouteFactory.create("/new-tags"));

        webTestClient
                .get()
                .uri("/new-tags/tag-slug-1")
                .exchange()
                .expectStatus()
                .isNotFound();

        Tag tag = new Tag();
        tag.setMetadata(new Metadata());
        tag.getMetadata().setName("fake-tag-name");
        tag.setSpec(new Tag.TagSpec());
        tag.getSpec().setSlug("tag-slug-2");
        when(client.listBy(eq(Tag.class), any(), any(PageRequest.class)))
                .thenReturn(Mono.just(new ListResult<>(List.of(tag))));
        when(tagFinder.getByName(eq(tag.getMetadata().getName()))).thenReturn(Mono.just(TagVo.from(tag)));
        webTestClient
                .get()
                .uri("/new-tags/tag-slug-2")
                .exchange()
                .expectStatus()
                .isOk();

        webTestClient
                .get()
                .uri("/new-tags/tag-slug-2/page/1")
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    void exposesCurrentResourceReferenceForLocalizedDiscovery() {
        var tag = new Tag();
        tag.setMetadata(new Metadata());
        tag.getMetadata().setName("fake-tag-name");
        tag.setSpec(new Tag.TagSpec());
        tag.getSpec().setSlug("tag-slug-2");
        when(client.listBy(eq(Tag.class), any(), any(PageRequest.class)))
                .thenReturn(Mono.just(new ListResult<>(List.of(tag))));
        when(tagFinder.getByName("fake-tag-name")).thenReturn(Mono.just(TagVo.from(tag)));
        when(viewResolver.resolveViewName(any(), any())).thenReturn(Mono.just(new EmptyView() {
            @Override
            public Mono<Void> render(Map<String, ?> model, MediaType contentType, ServerWebExchange exchange) {
                assertThat((Object) model.get(ModelConst.RESOURCE_REFERENCE))
                        .isEqualTo(new ResourceReference(GroupVersionKind.fromExtension(Tag.class), "fake-tag-name"));
                return super.render(model, contentType, exchange);
            }
        }));

        getWebTestClient(tagPostRouteFactory.create("/new-tags"))
                .get()
                .uri("/new-tags/tag-slug-2")
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    void declaresOwnershipOnlyForExistingTagRoutes() {
        when(client.countBy(eq(Tag.class), any())).thenReturn(Mono.just(1L));

        StepVerifier.create(tagPostRouteFactory.matches("/labels", "/labels/news", HttpMethod.GET))
                .expectNext(true)
                .verifyComplete();
        StepVerifier.create(tagPostRouteFactory.matches("/labels", "/labels/news/page/2", HttpMethod.GET))
                .expectNext(true)
                .verifyComplete();
        StepVerifier.create(tagPostRouteFactory.matches("/labels", "/labels/news/other", HttpMethod.GET))
                .expectNext(false)
                .verifyComplete();
        StepVerifier.create(tagPostRouteFactory.matches("/labels", "/labels/news", HttpMethod.POST))
                .expectNext(false)
                .verifyComplete();

        when(client.countBy(eq(Tag.class), any())).thenReturn(Mono.just(0L));
        StepVerifier.create(tagPostRouteFactory.matches("/labels", "/labels/missing", HttpMethod.GET))
                .expectNext(false)
                .verifyComplete();
    }
}
