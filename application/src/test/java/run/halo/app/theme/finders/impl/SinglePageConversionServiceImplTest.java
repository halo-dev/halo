package run.halo.app.theme.finders.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.content.ContentWrapper;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.extension.Metadata;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;
import run.halo.app.theme.ReactiveSinglePageContentHandler;

/**
 * Tests for {@link SinglePageConversionServiceImpl}.
 *
 * @author guqing
 * @since 2.7.0
 */
@ExtendWith(MockitoExtension.class)
class SinglePageConversionServiceImplTest {

    @Mock
    private ExtensionGetter extensionGetter;

    @InjectMocks
    private SinglePageConversionServiceImpl pageConversionService;

    @Test
    void extendPageContent() {
        when(extensionGetter.getEnabledExtensionByDefinition(
            eq(ReactiveSinglePageContentHandler.class)))
            .thenReturn(
                Flux.just(new PageContentHandlerB(),
                    new PageContentHandlerA(),
                    new PageContentHandlerC())
            );
        ContentWrapper contentWrapper = ContentWrapper.builder()
            .content("fake-content")
            .raw("fake-raw")
            .rawType("markdown")
            .build();
        SinglePage singlePage = new SinglePage();
        singlePage.setMetadata(new Metadata());
        singlePage.getMetadata().setName("fake-page");
        pageConversionService.extendPageContent(singlePage, contentWrapper)
            .as(StepVerifier::create)
            .consumeNextWith(contentVo -> {
                assertThat(contentVo.getContent()).isEqualTo("fake-content-B-A-C");
            })
            .verifyComplete();
    }

    static class PageContentHandlerA implements ReactiveSinglePageContentHandler {

        @Override
        public Mono<SinglePageContentContext> handle(
            @NonNull SinglePageContentContext pageContent) {
            pageContent.setContent(pageContent.getContent() + "-A");
            return Mono.just(pageContent);
        }
    }

    static class PageContentHandlerB implements ReactiveSinglePageContentHandler {

        @Override
        public Mono<SinglePageContentContext> handle(
            @NonNull SinglePageContentContext pageContent) {
            pageContent.setContent(pageContent.getContent() + "-B");
            return Mono.just(pageContent);
        }
    }

    static class PageContentHandlerC implements ReactiveSinglePageContentHandler {

        @Override
        public Mono<SinglePageContentContext> handle(
            @NonNull SinglePageContentContext pageContent) {
            pageContent.setContent(pageContent.getContent() + "-C");
            return Mono.just(pageContent);
        }
    }
}
