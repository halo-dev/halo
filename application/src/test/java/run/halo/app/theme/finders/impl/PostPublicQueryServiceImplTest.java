package run.halo.app.theme.finders.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
import run.halo.app.extension.Metadata;
import run.halo.app.localization.LocalizableDocument;
import run.halo.app.localization.LocalizationContext;
import run.halo.app.localization.LocalizationScope;
import run.halo.app.localization.ResourceLocalizationService;
import run.halo.app.localization.ResourceReference;
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

    @Mock
    private ResourceLocalizationService localizationService;

    @InjectMocks
    private PostPublicQueryServiceImpl postPublicQueryService;

    @Test
    void extendPostContent() {
        when(extensionGetter.getEnabledExtensions(eq(ReactivePostContentHandler.class)))
                .thenReturn(Flux.just(new PostContentHandlerB(), new PostContentHandlerA(), new PostContentHandlerC()));
        Post post = TestPost.postV1();
        post.getMetadata().setName("fake-post");
        ContentWrapper contentWrapper = ContentWrapper.builder()
                .content("fake-content")
                .raw("fake-raw")
                .rawType("markdown")
                .build();
        postPublicQueryService
                .extendPostContent(post, contentWrapper)
                .as(StepVerifier::create)
                .consumeNextWith(contentVo -> {
                    assertThat(contentVo.getContent()).isEqualTo("fake-content-B-A-C");
                })
                .verifyComplete();
    }

    @Test
    void contentHandlerReceivesLocalizedResourceAndContentTogether() {
        var source = new Post();
        var metadata = new Metadata();
        metadata.setName("content-name");
        source.setMetadata(metadata);
        source.setSpec(new Post.PostSpec());
        source.getSpec().setTitle("原题");
        var projected = new Post();
        projected.setMetadata(metadata);
        projected.setSpec(new Post.PostSpec());
        projected.getSpec().setTitle("Title");
        var context = new LocalizationContext(Locale.CHINA, Locale.ENGLISH, List.of(), LocalizationScope.PUBLIC_PAGE);
        var fields = Map.of("content.html", "<p>原文</p>", "content.raw", "原文");
        var translated = Map.of("content.html", "<p>Body</p>", "content.raw", "Body");
        when(localizationService.localize(source, context)).thenReturn(Mono.just(projected));
        when(localizationService.localizeFields(
                        ResourceReference.from(source),
                        fields,
                        List.of(new LocalizableDocument("content.html", "content.raw", "markdown", null)),
                        context))
                .thenReturn(Mono.just(translated));
        ReactivePostContentHandler handler = content -> {
            assertThat(content.getPost().getSpec().getTitle()).isEqualTo("Title");
            assertThat(content.getContent()).isEqualTo("<p>Body</p>");
            assertThat(content.getRaw()).isEqualTo("Body");
            assertThat(content.getRawType()).isEqualTo("markdown");
            content.setContent(content.getContent() + "-processed");
            return Mono.just(content);
        };
        when(extensionGetter.getEnabledExtensions(ReactivePostContentHandler.class))
                .thenReturn(Flux.just(handler));
        var wrapper = ContentWrapper.builder()
                .content("<p>原文</p>")
                .raw("原文")
                .rawType("markdown")
                .build();
        postPublicQueryService
                .extendPostContent(source, wrapper)
                .contextWrite(ctx -> ctx.put(LocalizationContext.class, context))
                .as(StepVerifier::create)
                .assertNext(content -> assertThat(content.getContent()).isEqualTo("<p>Body</p>-processed"))
                .verifyComplete();
        assertThat(source.getSpec().getTitle()).isEqualTo("原题");
    }

    @Test
    void contentLocalizationReceivesNullRawAndRetainsItWithoutATranslation() {
        var source = TestPost.postV1();
        source.getMetadata().setName("null-raw");
        var context = new LocalizationContext(Locale.CHINA, Locale.ENGLISH, List.of(), LocalizationScope.PUBLIC_PAGE);
        var fields = new LinkedHashMap<String, String>();
        fields.put("content.html", "<p>Original</p>");
        fields.put("content.raw", null);
        when(localizationService.localize(source, context)).thenReturn(Mono.just(source));
        when(localizationService.localizeFields(
                        ResourceReference.from(source),
                        fields,
                        List.of(new LocalizableDocument("content.html", "content.raw", "HTML", null)),
                        context))
                .thenReturn(Mono.just(fields));
        when(extensionGetter.getEnabledExtensions(ReactivePostContentHandler.class))
                .thenReturn(Flux.empty());
        var wrapper = ContentWrapper.builder()
                .content("<p>Original</p>")
                .rawType("HTML")
                .build();

        postPublicQueryService
                .extendPostContent(source, wrapper)
                .contextWrite(ctx -> ctx.put(LocalizationContext.class, context))
                .as(StepVerifier::create)
                .assertNext(content -> {
                    assertThat(content.getContent()).isEqualTo("<p>Original</p>");
                    assertThat(content.getRaw()).isNull();
                })
                .verifyComplete();

        verify(localizationService)
                .localizeFields(
                        ResourceReference.from(source),
                        fields,
                        List.of(new LocalizableDocument("content.html", "content.raw", "HTML", null)),
                        context);
        assertThat(wrapper.getRaw()).isNull();
    }

    @Test
    void blankSnapshotFormatRetainsSourceBodyAndLocalizedMetadata() {
        var source = TestPost.postV1();
        var projected = TestPost.postV1();
        projected.getSpec().setTitle("Localized title");
        var context = new LocalizationContext(Locale.CHINA, Locale.ENGLISH, List.of(), LocalizationScope.PUBLIC_PAGE);
        var wrapper = ContentWrapper.builder()
                .content("<p>Source</p>")
                .raw("Source")
                .rawType(" ")
                .build();
        when(localizationService.localize(source, context)).thenReturn(Mono.just(projected));
        ReactivePostContentHandler handler = content -> {
            assertThat(content.getPost().getSpec().getTitle()).isEqualTo("Localized title");
            assertThat(content.getRawType()).isEqualTo(" ");
            return Mono.just(content);
        };
        when(extensionGetter.getEnabledExtensions(ReactivePostContentHandler.class))
                .thenReturn(Flux.just(handler));

        StepVerifier.create(postPublicQueryService
                        .extendPostContent(source, wrapper)
                        .contextWrite(current -> current.put(LocalizationContext.class, context)))
                .assertNext(content -> {
                    assertThat(content.getContent()).isEqualTo("<p>Source</p>");
                    assertThat(content.getRaw()).isEqualTo("Source");
                })
                .verifyComplete();

        verify(localizationService).localize(source, context);
        verifyNoMoreInteractions(localizationService);
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
