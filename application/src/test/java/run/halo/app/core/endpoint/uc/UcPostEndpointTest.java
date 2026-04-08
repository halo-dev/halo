package run.halo.app.core.endpoint.uc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

import java.util.HashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import run.halo.app.content.PostRequest;
import run.halo.app.content.PostService;
import run.halo.app.content.SnapshotService;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.Metadata;

/**
 * Tests for {@link UcPostEndpoint}.
 */
@ExtendWith(MockitoExtension.class)
class UcPostEndpointTest {

    @Mock
    PostService postService;

    @Mock
    SnapshotService snapshotService;

    @InjectMocks
    UcPostEndpoint ucPostEndpoint;

    WebTestClient webClient;

    @BeforeEach
    void setUp() {
        webClient = WebTestClient.bindToRouterFunction(ucPostEndpoint.endpoint())
            .apply(springSecurity())
            .build();
    }

    @Nested
    class CreateMyPostTest {

        @Test
        void shouldForcePublishFalseWhenCreatingPost() {
            var createdPost = createPost("test-post", true);
            when(postService.draftPost(any(PostRequest.class))).thenReturn(Mono.just(createdPost));

            webClient.mutate()
                .apply(mockUser("test-user"))
                .build()
                .post()
                .uri("/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createPost("test-post", true))
                .exchange()
                .expectStatus()
                .isOk();

            // Verify that spec.publish is forced to false regardless of the request body value
            verify(postService).draftPost(assertArg(postRequest -> {
                var spec = postRequest.post().getSpec();
                assertThat(spec).isNotNull();
                assertThat(spec.getPublish()).isFalse();
            }));
        }

        @Test
        void shouldForcePublishFalseEvenWhenPublishTrueIsInRequestBody() {
            var postWithPublishTrue = createPost("post-with-publish", true);
            when(postService.draftPost(any(PostRequest.class)))
                .thenReturn(Mono.just(postWithPublishTrue));

            webClient.mutate()
                .apply(mockUser("test-user"))
                .build()
                .post()
                .uri("/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(postWithPublishTrue)
                .exchange()
                .expectStatus()
                .isOk();

            verify(postService).draftPost(assertArg(postRequest -> {
                var spec = postRequest.post().getSpec();
                assertThat(spec.getPublish()).isFalse();
            }));
        }

        @Test
        void shouldSetOwnerToCurrentUser() {
            var post = createPost("my-post", false);
            when(postService.draftPost(any(PostRequest.class))).thenReturn(Mono.just(post));

            webClient.mutate()
                .apply(mockUser("current-user"))
                .build()
                .post()
                .uri("/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(post)
                .exchange()
                .expectStatus()
                .isOk();

            verify(postService).draftPost(assertArg(postRequest -> {
                var spec = postRequest.post().getSpec();
                assertThat(spec.getOwner()).isEqualTo("current-user");
                assertThat(spec.getPublish()).isFalse();
            }));
        }

        private Post createPost(String name, boolean publish) {
            var post = new Post();
            post.setApiVersion("content.halo.run/v1alpha1");
            post.setKind("Post");
            var metadata = new Metadata();
            metadata.setName(name);
            var annotations = new HashMap<String, String>();
            // Provide content JSON annotation as required by createMyPost
            annotations.put("content.halo.run/content-json",
                "{\"raw\":\"\",\"content\":\"\",\"rawType\":\"markdown\"}");
            metadata.setAnnotations(annotations);
            post.setMetadata(metadata);
            var spec = new Post.PostSpec();
            spec.setTitle("Test Post");
            spec.setSlug("test-post");
            spec.setPublish(publish);
            post.setSpec(spec);
            return post;
        }
    }

    @Nested
    class UpdateMyPostTest {

        @Test
        void shouldPreservePublishStateFromOldPost() {
            var oldPost = createPost("test-post", true);
            oldPost.getMetadata().setVersion(1L);

            when(postService.getByUsername("test-post", "test-user"))
                .thenReturn(Mono.just(oldPost));
            when(postService.updateBy(any(Post.class))).thenReturn(Mono.just(oldPost));

            // Request body has publish=false, but server should preserve publish=true from old post
            var updateRequest = createPost("test-post", false);

            webClient.mutate()
                .apply(mockUser("test-user"))
                .build()
                .put()
                .uri("/posts/test-post")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus()
                .isOk();

            // Verify that spec.publish is preserved from the old post (true), not from the request
            verify(postService).updateBy(assertArg(post -> {
                var spec = post.getSpec();
                assertThat(spec.getPublish()).isTrue();
            }));
        }

        @Test
        void shouldPreserveDraftStateFromOldPost() {
            var oldPost = createPost("draft-post", false);
            oldPost.getMetadata().setVersion(1L);

            when(postService.getByUsername("draft-post", "test-user"))
                .thenReturn(Mono.just(oldPost));
            when(postService.updateBy(any(Post.class))).thenReturn(Mono.just(oldPost));

            // Request body has publish=true, but server should preserve publish=false from old post
            var updateRequest = createPost("draft-post", true);

            webClient.mutate()
                .apply(mockUser("test-user"))
                .build()
                .put()
                .uri("/posts/draft-post")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus()
                .isOk();

            // Verify that spec.publish is preserved from the old post (false)
            verify(postService).updateBy(assertArg(post -> {
                var spec = post.getSpec();
                assertThat(spec.getPublish()).isFalse();
            }));
        }

        private Post createPost(String name, boolean publish) {
            var post = new Post();
            post.setApiVersion("content.halo.run/v1alpha1");
            post.setKind("Post");
            var metadata = new Metadata();
            metadata.setName(name);
            post.setMetadata(metadata);
            var spec = new Post.PostSpec();
            spec.setTitle("Test Post");
            spec.setSlug(name);
            spec.setPublish(publish);
            post.setSpec(spec);
            return post;
        }
    }
}
