package run.halo.app.search.post;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.content.ContentWrapper;
import run.halo.app.content.PostService;
import run.halo.app.core.extension.content.Post;
import run.halo.app.event.post.PostDeletedEvent;
import run.halo.app.event.post.PostUnpublishedEvent;
import run.halo.app.event.post.PostUpdatedEvent;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.search.HaloDocument;
import run.halo.app.search.HaloDocumentEventsListener;

@ExtendWith(MockitoExtension.class)
class PostEventsListenerTest {

    private static final String POST_NAME = "post";
    private static final String DOCUMENT_ID = PostHaloDocumentsProvider.POST_DOCUMENT_TYPE + "-" + POST_NAME;

    @Mock
    HaloDocumentEventsListener indices;

    @Mock
    PostService postService;

    @Mock
    ReactiveExtensionClient client;

    @InjectMocks
    PostEventsListener listener;

    @Captor
    ArgumentCaptor<Supplier<Mono<HaloDocument>>> load;

    @Test
    void readsCurrentPostOnlyWhenTheQueuedRefreshRuns() {
        listener.onApplicationEvent(new PostUpdatedEvent(this, POST_NAME));
        verify(indices).refreshDocument(eq(DOCUMENT_ID), load.capture());
        verifyNoInteractions(client, postService);

        var post = post();
        when(client.fetch(Post.class, POST_NAME)).thenReturn(Mono.just(post));
        when(postService.getReleaseContent(post))
                .thenReturn(
                        Mono.just(ContentWrapper.builder().content("release").build()));
        StepVerifier.create(load.getValue().get())
                .assertNext(document -> {
                    assertThat(document.getId()).isEqualTo(DOCUMENT_ID);
                    assertThat(document.getContent()).isEqualTo("release");
                })
                .verifyComplete();
    }

    @Test
    void omitsDeletingPostAndNeverLoadsItsRelease() {
        listener.onApplicationEvent(new PostUpdatedEvent(this, POST_NAME));
        verify(indices).refreshDocument(eq(DOCUMENT_ID), load.capture());
        var post = post();
        post.getMetadata().setDeletionTimestamp(Instant.now());
        when(client.fetch(Post.class, POST_NAME)).thenReturn(Mono.just(post));

        StepVerifier.create(load.getValue().get()).verifyComplete();
        verifyNoInteractions(postService);
    }

    @Test
    void missingReleaseRemovesPreviousIndexedDocument() {
        listener.onApplicationEvent(new PostUnpublishedEvent(this, POST_NAME));
        verify(indices).refreshDocument(eq(DOCUMENT_ID), load.capture());
        var post = post();
        when(client.fetch(Post.class, POST_NAME)).thenReturn(Mono.just(post));
        when(postService.getReleaseContent(post)).thenReturn(Mono.empty());

        StepVerifier.create(load.getValue().get()).verifyComplete();
    }

    @Test
    void deletionQueuesRemovalWithoutReadingStorage() {
        listener.onApplicationEvent(new PostDeletedEvent(this, post()));
        verify(indices).deleteDocuments(List.of(DOCUMENT_ID));
        verifyNoInteractions(client, postService);
    }

    private static Post post() {
        var post = new Post();
        var metadata = new Metadata();
        metadata.setName(POST_NAME);
        post.setMetadata(metadata);
        post.setSpec(new Post.PostSpec());
        post.setStatus(new Post.PostStatus());
        return post;
    }
}
