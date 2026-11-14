package run.halo.app.core.endpoint.uc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import run.halo.app.content.PostRequest;
import run.halo.app.content.PostService;
import run.halo.app.content.SnapshotService;
import run.halo.app.content.TestPost;
import run.halo.app.core.extension.content.Constant;
import run.halo.app.core.extension.content.Post;

@ExtendWith(MockitoExtension.class)
class UcPostEndpointTest {

    @Mock
    PostService postService;

    @Mock
    SnapshotService snapshotService;

    WebTestClient webClient;

    @BeforeEach
    void setUp() {
        var endpoint = new UcPostEndpoint(postService, snapshotService);
        webClient = WebTestClient.bindToRouterFunction(endpoint.endpoint())
                .apply(springSecurity())
                .build()
                .mutate()
                .apply(mockUser("contributor"))
                .build();
    }

    @Test
    void createIgnoresPublishingAndManagedFields() {
        var submitted = TestPost.postV1();
        submitted.getMetadata().setLabels(Map.of(Post.PUBLISHED_LABEL, "true"));
        submitted.getMetadata().setFinalizers(Set.of("post-protection"));
        submitted
                .getMetadata()
                .setAnnotations(new HashMap<>(Map.of(
                        "content.halo.run/content-json",
                        """
                    {"raw":"text","content":"<p>text</p>","rawType":"HTML"}
                    """,
                        Constant.CONTENT_CHECKSUM_ANNO,
                        "forged",
                        "custom.example/key",
                        "value")));
        submitted.getSpec().setOwner("someone-else");
        submitted.getSpec().setPublish(true);
        submitted.getSpec().setPinned(true);
        submitted.getSpec().setPriority(100);
        submitted.getSpec().setDeleted(true);
        submitted.getSpec().setHeadSnapshot("someone-else-head");
        submitted.getSpec().setBaseSnapshot("someone-else-base");
        submitted.getSpec().setReleaseSnapshot("someone-else-snapshot");
        submitted.setStatus(new Post.PostStatus());

        when(postService.draftPost(any())).thenAnswer(invocation -> {
            PostRequest request = invocation.getArgument(0);
            return Mono.just(request.post());
        });

        webClient
                .post()
                .uri("/posts")
                .bodyValue(submitted)
                .exchange()
                .expectStatus()
                .isOk();

        var captor = ArgumentCaptor.forClass(PostRequest.class);
        verify(postService).draftPost(captor.capture());
        var created = captor.getValue().post();
        assertThat(created.getSpec().getOwner()).isEqualTo("contributor");
        assertThat(created.getSpec().getPublish()).isFalse();
        assertThat(created.getSpec().getPinned()).isFalse();
        assertThat(created.getSpec().getPriority()).isZero();
        assertThat(created.getSpec().getDeleted()).isFalse();
        assertThat(created.getSpec().getHeadSnapshot()).isNull();
        assertThat(created.getSpec().getBaseSnapshot()).isNull();
        assertThat(created.getSpec().getReleaseSnapshot()).isNull();
        assertThat(created.getMetadata().getLabels()).isNull();
        assertThat(created.getMetadata().getFinalizers()).isNull();
        assertThat(created.getMetadata().getAnnotations())
                .containsEntry("custom.example/key", "value")
                .doesNotContainKey(Constant.CONTENT_CHECKSUM_ANNO);
        assertThat(created.getStatus()).isNull();
        assertThat(captor.getValue().content()).isNotNull();
    }

    @Test
    void updatePreservesManagedFieldsAndSavesEditableFields() {
        var existing = TestPost.postV1();
        existing.getSpec().setOwner("contributor");
        existing.getSpec().setPublish(false);
        existing.getSpec().setPinned(true);
        existing.getSpec().setPriority(7);
        existing.getSpec().setDeleted(false);
        existing.getMetadata().setLabels(Map.of(Post.PUBLISHED_LABEL, "false"));
        existing.getMetadata().setAnnotations(Map.of("content.halo.run/permalink-pattern", "/posts/{slug}"));
        existing.getMetadata().setFinalizers(Set.of("post-protection"));
        var existingStatus = new Post.PostStatus();
        existingStatus.setPhase(Post.PostPhase.DRAFT.name());
        existing.setStatus(existingStatus);

        var submitted = TestPost.postV1();
        submitted.getSpec().setTitle("updated title");
        submitted.getSpec().setOwner("someone-else");
        submitted.getSpec().setPublish(true);
        submitted.getSpec().setPinned(false);
        submitted.getSpec().setPriority(100);
        submitted.getSpec().setReleaseSnapshot("someone-else-snapshot");
        submitted.getMetadata().setLabels(Map.of(Post.PUBLISHED_LABEL, "true"));
        submitted.getMetadata().setFinalizers(Set.of("forged"));
        submitted
                .getMetadata()
                .setAnnotations(new HashMap<>(
                        Map.of("content.halo.run/permalink-pattern", "/forged", "custom.example/key", "value")));
        var forgedStatus = new Post.PostStatus();
        forgedStatus.setPhase(Post.PostPhase.PUBLISHED.name());
        submitted.setStatus(forgedStatus);

        when(postService.getByUsername(eq("post-A"), eq("contributor"))).thenReturn(Mono.just(existing));
        when(postService.updateBy(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        webClient
                .put()
                .uri("/posts/post-A")
                .bodyValue(submitted)
                .exchange()
                .expectStatus()
                .isOk();

        var captor = ArgumentCaptor.forClass(Post.class);
        verify(postService).updateBy(captor.capture());
        var updated = captor.getValue();
        assertThat(updated.getSpec().getTitle()).isEqualTo("updated title");
        assertThat(updated.getSpec().getOwner()).isEqualTo("contributor");
        assertThat(updated.getSpec().getPublish()).isFalse();
        assertThat(updated.getSpec().getPinned()).isTrue();
        assertThat(updated.getSpec().getPriority()).isEqualTo(7);
        assertThat(updated.getSpec().getReleaseSnapshot()).isNull();
        assertThat(updated.getMetadata().getLabels()).containsEntry(Post.PUBLISHED_LABEL, "false");
        assertThat(updated.getMetadata().getFinalizers()).containsExactly("post-protection");
        assertThat(updated.getMetadata().getAnnotations())
                .containsEntry("content.halo.run/permalink-pattern", "/posts/{slug}")
                .containsEntry("custom.example/key", "value");
        assertThat(updated.getStatus().getPhase()).isEqualTo(Post.PostPhase.DRAFT.name());
    }
}
