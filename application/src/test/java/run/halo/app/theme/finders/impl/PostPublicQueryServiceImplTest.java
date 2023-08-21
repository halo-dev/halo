package run.halo.app.theme.finders.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
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
import run.halo.app.content.TestPost;
import run.halo.app.core.extension.content.Post;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;
import run.halo.app.theme.ReactivePostContentHandler;

/**
 * Tests for {@link PostPublicQueryServiceImpl}.
 *
 * @author guqing
 * @since 2.7.0
 */
@ExtendWith(MockitoExtension.class)
class PostPublicQueryServiceImplTest {

    @Mock
    private ExtensionGetter extensionGetter;

    @InjectMocks
    private PostPublicQueryServiceImpl postPublicQueryService;

    @Test
    void extendPostContent() {
        when(extensionGetter.getEnabledExtensionByDefinition(
            eq(ReactivePostContentHandler.class))).thenReturn(
            Flux.just(new PostContentHandlerB(), new PostContentHandlerA(),
                new PostContentHandlerC()));
        Post post = TestPost.postV1();
        post.getMetadata().setName("fake-post");
        ContentWrapper contentWrapper =
            ContentWrapper.builder().content("fake-content").raw("fake-raw").rawType("markdown")
                .build();
        postPublicQueryService.extendPostContent(post, contentWrapper)
            .as(StepVerifier::create).consumeNextWith(contentVo -> {
                assertThat(contentVo.getContent()).isEqualTo("fake-content-B-A-C");
            }).verifyComplete();
    }

    static class PostContentHandlerA implements ReactivePostContentHandler {

        @Override
        public Mono<PostContentContext> handle(PostContentContext postContent) {
            postContent.setContent(postContent.getContent() + "-A");
            return Mono.just(postContent);
        }
    }

    static class PostContentHandlerB implements ReactivePostContentHandler {

        @Override
        public Mono<PostContentContext> handle(PostContentContext postContent) {
            postContent.setContent(postContent.getContent() + "-B");
            return Mono.just(postContent);
        }
    }

    static class PostContentHandlerC implements ReactivePostContentHandler {

        @Override
        public Mono<PostContentContext> handle(PostContentContext postContent) {
            postContent.setContent(postContent.getContent() + "-C");
            return Mono.just(postContent);
        }
    }
}
