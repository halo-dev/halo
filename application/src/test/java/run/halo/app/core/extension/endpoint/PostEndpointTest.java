package run.halo.app.core.extension.endpoint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import run.halo.app.content.PostRequest;
import run.halo.app.content.PostService;
import run.halo.app.content.TestPost;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.content.Post.PostSpec;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;

/**
 * Tests for @{@link PostEndpoint}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class PostEndpointTest {

    @Mock
    PostService postService;
    @Mock
    ReactiveExtensionClient client;

    @Mock
    ApplicationEventPublisher eventPublisher;

    @InjectMocks
    PostEndpoint postEndpoint;

    WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient
            .bindToRouterFunction(postEndpoint.endpoint())
            .build();
    }

    @Test
    void draftPost() {
        when(postService.draftPost(any())).thenReturn(Mono.just(TestPost.postV1()));
        webTestClient.post()
            .uri("/posts")
            .bodyValue(postRequest(TestPost.postV1()))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(Post.class)
            .value(post -> assertThat(post).isEqualTo(TestPost.postV1()));
    }

    @Test
    void updatePost() {
        when(postService.updatePost(any())).thenReturn(Mono.just(TestPost.postV1()));

        webTestClient.put()
            .uri("/posts/post-A")
            .bodyValue(postRequest(TestPost.postV1()))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(Post.class)
            .value(post -> assertThat(post).isEqualTo(TestPost.postV1()));
    }

    @Test
    void publishRetryOnOptimisticLockingFailure() {
        var post = new Post();
        post.setMetadata(new Metadata());
        post.getMetadata().setName("post-1");
        post.setSpec(new PostSpec());
        when(client.get(eq(Post.class), eq("post-1"))).thenReturn(Mono.just(post));

        when(client.update(any(Post.class)))
            .thenReturn(Mono.error(new OptimisticLockingFailureException("fake-error")));

        // Send request
        webTestClient.put()
            .uri("/posts/{name}/publish?async=false", "post-1")
            .exchange()
            .expectStatus()
            .is5xxServerError();

        // Verify WebClient retry behavior
        verify(client, times(6)).get(eq(Post.class), eq("post-1"));
        verify(client, times(6)).update(any(Post.class));
    }

    @Test
    void publishSuccess() {
        var post = new Post();
        post.setMetadata(new Metadata());
        post.getMetadata().setName("post-1");
        post.setSpec(new PostSpec());

        var publishedPost = new Post();
        var publishedMetadata = new Metadata();
        publishedMetadata.setAnnotations(Map.of(Post.LAST_RELEASED_SNAPSHOT_ANNO, "my-release"));
        publishedPost.setMetadata(publishedMetadata);
        var publishedPostSpec = new PostSpec();
        publishedPostSpec.setReleaseSnapshot("my-release");
        publishedPost.setSpec(publishedPostSpec);

        when(client.get(eq(Post.class), eq("post-1")))
            .thenReturn(Mono.just(post))
            .thenReturn(Mono.just(publishedPost));

        when(client.update(any(Post.class)))
            .thenReturn(Mono.just(post));

        // Send request
        webTestClient.put()
            .uri("/posts/{name}/publish?async=false", "post-1")
            .exchange()
            .expectStatus()
            .is2xxSuccessful();

        // Verify WebClient retry behavior
        verify(client, times(2)).get(eq(Post.class), eq("post-1"));
        verify(client).update(any(Post.class));
    }

    @Test
    void shouldFailIfWaitTimeoutForPublishedStatus() {
        var post = new Post();
        post.setMetadata(new Metadata());
        post.getMetadata().setName("post-1");
        post.setSpec(new PostSpec());

        var publishedPost = new Post();
        var publishedMetadata = new Metadata();
        publishedMetadata.setAnnotations(
            Map.of(Post.LAST_RELEASED_SNAPSHOT_ANNO, "old-my-release"));
        publishedPost.setMetadata(publishedMetadata);
        var publishedPostSpec = new PostSpec();
        publishedPostSpec.setReleaseSnapshot("my-release");
        publishedPost.setSpec(publishedPostSpec);

        when(client.get(eq(Post.class), eq("post-1")))
            .thenReturn(Mono.just(post))
            .thenReturn(Mono.just(publishedPost));

        when(client.update(any(Post.class)))
            .thenReturn(Mono.just(post));

        // Send request
        webTestClient.put()
            .uri("/posts/{name}/publish?async=false", "post-1")
            .exchange()
            .expectStatus()
            .is5xxServerError();

        // Verify WebClient retry behavior
        verify(client, times(12)).get(eq(Post.class), eq("post-1"));
        verify(client).update(any(Post.class));
    }

    PostRequest postRequest(Post post) {
        return new PostRequest(post, new PostRequest.Content("B", "<p>B</p>", "MARKDOWN"));
    }
}