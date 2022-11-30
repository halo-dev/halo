package run.halo.app.core.extension.endpoint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import run.halo.app.content.PostRequest;
import run.halo.app.content.PostService;
import run.halo.app.content.TestPost;
import run.halo.app.core.extension.content.Post;
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

    PostRequest postRequest(Post post) {
        return new PostRequest(post, new PostRequest.Content("B", "<p>B</p>", "MARKDOWN"));
    }
}