package run.halo.app.core.extension.endpoint;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;
import static org.springframework.test.web.reactive.server.WebTestClient.bindToRouterFunction;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Tag;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.PageRequest;
import run.halo.app.extension.ReactiveExtensionClient;

/**
 * Tag endpoint test.
 *
 * @author LIlGG
 */
@ExtendWith(MockitoExtension.class)
class TagEndpointTest {
    @Mock
    ReactiveExtensionClient client;

    @InjectMocks
    TagEndpoint tagEndpoint;

    WebTestClient webClient;

    @BeforeEach
    void setUp() {
        webClient = WebTestClient.bindToRouterFunction(tagEndpoint.endpoint())
            .apply(springSecurity())
            .build();
    }

    @Nested
    class TagListTest {

        @Test
        void shouldListEmptyTagsWhenNoTags() {
            when(client.listBy(same(Tag.class), any(), any(PageRequest.class)))
                .thenReturn(Mono.just(ListResult.emptyResult()));

            bindToRouterFunction(tagEndpoint.endpoint())
                .build()
                .get().uri("/tags")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.items.length()").isEqualTo(0)
                .jsonPath("$.total").isEqualTo(0);
        }

        @Test
        void shouldListTagsWhenTagPresent() {
            var tags = List.of(
                createTag("fake-tag-1"),
                createTag("fake-tag-2")
            );
            var expectResult = new ListResult<>(tags);
            when(client.listBy(same(Tag.class), any(), any(PageRequest.class)))
                .thenReturn(Mono.just(expectResult));

            bindToRouterFunction(tagEndpoint.endpoint())
                .build()
                .get().uri("/tags")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.items.length()").isEqualTo(2)
                .jsonPath("$.total").isEqualTo(2);
        }

        Tag createTag(String name) {
            return createTag(name, "fake display name");
        }

        Tag createTag(String name, String displayName) {
            var metadata = new Metadata();
            metadata.setName(name);
            metadata.setCreationTimestamp(Instant.now());
            var spec = new Tag.TagSpec();
            spec.setDisplayName(displayName);
            spec.setSlug(name);
            var tag = new Tag();
            tag.setMetadata(metadata);
            tag.setSpec(spec);
            return tag;
        }

    }
}
