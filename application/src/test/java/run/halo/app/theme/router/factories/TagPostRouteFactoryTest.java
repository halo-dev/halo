package run.halo.app.theme.router.factories;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Tag;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.theme.finders.PostFinder;
import run.halo.app.theme.finders.TagFinder;
import run.halo.app.theme.finders.vo.TagVo;

/**
 * Tests for @link TagPostRouteFactory}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class TagPostRouteFactoryTest extends RouteFactoryTestSuite {
    @Mock
    private ReactiveExtensionClient client;
    @Mock
    private TagFinder tagFinder;
    @Mock
    private PostFinder postFinder;

    @InjectMocks
    TagPostRouteFactory tagPostRouteFactory;

    @Test
    void create() {
        when(client.list(eq(Tag.class), any(), eq(null))).thenReturn(Flux.empty());
        WebTestClient webTestClient = getWebTestClient(tagPostRouteFactory.create("/new-tags"));

        webTestClient.get()
            .uri("/new-tags/tag-slug-1")
            .exchange()
            .expectStatus().isNotFound();

        Tag tag = new Tag();
        tag.setMetadata(new Metadata());
        tag.getMetadata().setName("fake-tag-name");
        tag.setSpec(new Tag.TagSpec());
        tag.getSpec().setSlug("tag-slug-2");
        when(client.list(eq(Tag.class), any(), eq(null))).thenReturn(Flux.just(tag));
        when(tagFinder.getByName(eq(tag.getMetadata().getName())))
            .thenReturn(Mono.just(TagVo.from(tag)));
        webTestClient.get()
            .uri("/new-tags/tag-slug-2")
            .exchange()
            .expectStatus().isOk();

        webTestClient.get()
            .uri("/new-tags/tag-slug-2/page/1")
            .exchange()
            .expectStatus().isOk();
    }
}