package run.halo.app.search.post;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.content.ContentWrapper;
import run.halo.app.content.PostService;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.Metadata;
import run.halo.app.infra.ReactiveExtensionPaginatedOperator;

@ExtendWith(MockitoExtension.class)
class PostHaloDocumentsProviderTest {

    @Mock
    PostService postService;

    @Mock
    ReactiveExtensionPaginatedOperator paginatedOperator;

    @InjectMocks
    PostHaloDocumentsProvider provider;

    @Test
    void ensureTypeNotModified() {
        assertEquals("post.content.halo.run", provider.getType());
    }

    @Test
    void shouldFetchAll() {
        var post = createFakePost();
        when(paginatedOperator.list(same(Post.class), any(ListOptions.class)))
            .thenReturn(Flux.just(post));
        var content = ContentWrapper.builder()
            .content("fake-content")
            .raw("fake-content")
            .build();
        when(postService.getReleaseContent(post)).thenReturn(Mono.just(content));
        provider.fetchAll()
            .as(StepVerifier::create)
            .assertNext(doc -> {
                assertEquals("post.content.halo.run", doc.getType());
                assertEquals("fake-post", doc.getMetadataName());
                assertEquals("post.content.halo.run-fake-post", doc.getId());
                assertEquals("fake-content", doc.getContent());
            })
            .verifyComplete();
    }

    @Test
    void shouldFetchAllIfNoContent() {
        var post = createFakePost();
        when(paginatedOperator.list(same(Post.class), any(ListOptions.class)))
            .thenReturn(Flux.just(post));
        when(postService.getReleaseContent(post)).thenReturn(Mono.empty());
        provider.fetchAll()
            .as(StepVerifier::create)
            .assertNext(doc -> {
                assertEquals("post.content.halo.run", doc.getType());
                assertEquals("fake-post", doc.getMetadataName());
                assertEquals("post.content.halo.run-fake-post", doc.getId());
                assertEquals("", doc.getContent());
            })
            .verifyComplete();
    }

    Post createFakePost() {
        var post = new Post();
        var metadata = new Metadata();
        metadata.setName("fake-post");
        post.setMetadata(metadata);
        var spec = new Post.PostSpec();
        var status = new Post.PostStatus();
        post.setSpec(spec);
        post.setStatus(status);
        return post;
    }
}