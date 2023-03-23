package run.halo.app.content.comment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.content.TestPost;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.FakeExtension;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Ref;

/**
 * Tests for {@link PostCommentSubject}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class PostCommentSubjectTest {
    @Mock
    private ReactiveExtensionClient client;

    @InjectMocks
    private PostCommentSubject postCommentSubject;

    @Test
    void get() {
        when(client.fetch(eq(Post.class), any()))
            .thenReturn(Mono.empty());
        when(client.fetch(eq(Post.class), eq("fake-post")))
            .thenReturn(Mono.just(TestPost.postV1()));

        postCommentSubject.get("fake-post")
            .as(StepVerifier::create)
            .expectNext(TestPost.postV1())
            .verifyComplete();

        postCommentSubject.get("fake-post2")
            .as(StepVerifier::create)
            .verifyComplete();
    }

    @Test
    void supports() {
        Post post = new Post();
        post.setMetadata(new Metadata());
        post.getMetadata().setName("test");
        boolean supports = postCommentSubject.supports(Ref.of(post));
        assertThat(supports).isTrue();

        FakeExtension fakeExtension = new FakeExtension();
        fakeExtension.setMetadata(new Metadata());
        fakeExtension.getMetadata().setName("test");
        supports = postCommentSubject.supports(Ref.of(fakeExtension));
        assertThat(supports).isFalse();
    }
}