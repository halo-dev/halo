package run.halo.app.search.post;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.content.ContentWrapper;
import run.halo.app.content.PostService;
import run.halo.app.core.extension.content.Post;
import run.halo.app.event.post.PostDeletedEvent;
import run.halo.app.event.post.PostUpdatedEvent;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.search.event.HaloDocumentAddRequestEvent;
import run.halo.app.search.event.HaloDocumentDeleteRequestEvent;

@ExtendWith(MockitoExtension.class)
class PostEventsListenerTest {

    @Mock
    ApplicationEventPublisher publisher;

    @Mock
    PostService postService;

    @Mock
    ReactiveExtensionClient client;

    @InjectMocks
    PostEventsListener listener;

    @Nested
    class PostUpdatedEventTest {

        @Test
        void shouldDoNothingIfPostIsDeleted() {
            when(client.fetch(Post.class, "fake-post"))
                .thenReturn(Mono.empty());
            var event = new PostUpdatedEvent(this, "fake-post");
            listener.onApplicationEvent(event)
                .as(StepVerifier::create)
                .verifyComplete();

            verify(publisher, never()).publishEvent(any());
        }

        @Test
        void shouldRequestDeleteWhilePostIsDeleting() {
            var post = new Post();
            var metadata = new Metadata();
            metadata.setName("fake-post");
            metadata.setDeletionTimestamp(Instant.now());
            post.setMetadata(metadata);
            when(client.fetch(Post.class, "fake-post"))
                .thenReturn(Mono.just(post));
            var event = new PostUpdatedEvent(this, "fake-post");
            listener.onApplicationEvent(event)
                .as(StepVerifier::create)
                .verifyComplete();

            verify(publisher).publishEvent(
                assertArg(e -> assertInstanceOf(HaloDocumentDeleteRequestEvent.class, e))
            );
        }

        @Test
        void shouldRequestAddWhilePostIsNotDeleted() {
            var post = new Post();
            var metadata = new Metadata();
            metadata.setName("fake-post");
            post.setMetadata(metadata);
            var spec = new Post.PostSpec();
            post.setSpec(spec);
            var status = new Post.PostStatus();
            post.setStatus(status);
            when(client.fetch(Post.class, "fake-post"))
                .thenReturn(Mono.just(post));
            var content = ContentWrapper.builder()
                .content("fake-content")
                .raw("fake-content")
                .build();
            when(postService.getReleaseContent(post)).thenReturn(Mono.just(content));
            var event = new PostUpdatedEvent(this, "fake-post");
            listener.onApplicationEvent(event)
                .as(StepVerifier::create)
                .verifyComplete();

            verify(publisher).publishEvent(
                assertArg(e -> assertInstanceOf(HaloDocumentAddRequestEvent.class, e))
            );
        }
    }

    @Nested
    class PostDeleteEventTest {

        @Test
        void shouldRequestDelete() {
            var post = new Post();
            var metadata = new Metadata();
            metadata.setName("fake-post");
            post.setMetadata(metadata);
            var event = new PostDeletedEvent(this, post);
            listener.onApplicationEvent(event);

            verify(publisher).publishEvent(
                assertArg(e -> assertInstanceOf(HaloDocumentDeleteRequestEvent.class, e))
            );
        }
    }
}