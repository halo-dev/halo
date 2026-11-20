package run.halo.app.localization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.context.Context;
import run.halo.app.core.extension.content.Tag;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Unstructured;

class DefaultReactivePresentationExtensionClientTest {
    private final ReactiveExtensionClient sources = mock(ReactiveExtensionClient.class);
    private final ResourcePresentationService presentation = mock(ResourcePresentationService.class);
    private final DefaultReactivePresentationExtensionClient client =
            new DefaultReactivePresentationExtensionClient(sources, presentation);
    private final LocalizationContext publicContext =
            new LocalizationContext(Locale.CHINA, Locale.ENGLISH, List.of(), LocalizationScope.PUBLIC_API);

    @Test
    void sourceReadsRetainStreamingCancellationAndDoNotBuffer() {
        var emitted = new AtomicInteger();
        var source = new Tag();
        var options = new ListOptions();
        when(sources.listAll(Tag.class, options, Sort.unsorted()))
                .thenReturn(Flux.just(source)
                        .doOnNext(ignored -> emitted.incrementAndGet())
                        .concatWith(Flux.never()));
        client.listAll(Tag.class, options, Sort.unsorted())
                .next()
                .as(StepVerifier::create)
                .expectNext(source)
                .expectComplete()
                .verify(Duration.ofSeconds(1));
        assertThat(emitted).hasValue(1);
        verifyNoInteractions(presentation);
    }

    @Test
    void nonPublicContextsDoNotRunDisplayProcessors() {
        var source = new Tag();
        when(sources.fetch(Tag.class, "tag")).thenReturn(Mono.just(source));
        for (var scope :
                List.of(LocalizationScope.CONSOLE, LocalizationScope.INTERNAL, LocalizationScope.SEARCH_INDEX)) {
            var context = new LocalizationContext(Locale.CHINA, Locale.ENGLISH, List.of(), scope);
            client.fetch(Tag.class, "tag")
                    .contextWrite(Context.of(LocalizationContext.class, context))
                    .as(StepVerifier::create)
                    .expectNext(source)
                    .verifyComplete();
        }
        verifyNoInteractions(presentation);
    }

    @Test
    void sourceErrorsAndNotFoundAreNotTurnedIntoSuccess() {
        var error = new IllegalStateException("source failed");
        when(sources.get(Tag.class, "tag")).thenReturn(Mono.error(error));
        client.get(Tag.class, "tag")
                .contextWrite(Context.of(LocalizationContext.class, publicContext))
                .as(StepVerifier::create)
                .expectErrorMatches(value -> value == error)
                .verify();
        verifyNoInteractions(presentation);
    }

    @Test
    void pagesPreserveTotalsOrderAndBoundedBatches() {
        var originals = java.util.stream.IntStream.range(0, 205)
                .mapToObj(ignored -> new Tag())
                .toList();
        var options = new ListOptions();
        var request = PageRequestImpl.of(3, 205);
        when(sources.listBy(Tag.class, options, request))
                .thenReturn(Mono.just(new ListResult<>(3, 205, 900L, originals)));
        var sizes = new ArrayList<Integer>();
        when(presentation.presentAll(anyList())).thenAnswer(invocation -> {
            List<Tag> batch = invocation.getArgument(0);
            sizes.add(batch.size());
            return Mono.just(batch);
        });
        client.listBy(Tag.class, options, request)
                .contextWrite(Context.of(LocalizationContext.class, publicContext))
                .as(StepVerifier::create)
                .assertNext(page -> {
                    assertThat(page.getItems()).containsExactlyElementsOf(originals);
                    assertThat(page.getPage()).isEqualTo(3);
                    assertThat(page.getSize()).isEqualTo(205);
                    assertThat(page.getTotal()).isEqualTo(900);
                })
                .verifyComplete();
        assertThat(sizes).containsExactly(100, 100, 5);
    }

    @Test
    void localeIsSelectedForEachSubscription() {
        var source = new Tag();
        var translated = new Tag();
        when(sources.fetch(Tag.class, "tag")).thenReturn(Mono.just(source));
        when(presentation.presentAll(List.of(source))).thenReturn(Mono.just(List.of(translated)));
        var result = client.fetch(Tag.class, "tag");
        result.contextWrite(Context.of(LocalizationContext.class, publicContext))
                .as(StepVerifier::create)
                .expectNext(translated)
                .verifyComplete();
        result.as(StepVerifier::create).expectNext(source).verifyComplete();
    }

    @Test
    void countsAndIdentitiesBypassPresentation() {
        var options = new ListOptions();
        when(sources.countBy(Tag.class, options)).thenReturn(Mono.just(21L));
        when(sources.listAllNames(Tag.class, options, Sort.unsorted())).thenReturn(Flux.just("one", "two"));
        client.countBy(Tag.class, options)
                .contextWrite(Context.of(LocalizationContext.class, publicContext))
                .as(StepVerifier::create)
                .expectNext(21L)
                .verifyComplete();
        client.listAllNames(Tag.class, options, Sort.unsorted())
                .contextWrite(Context.of(LocalizationContext.class, publicContext))
                .as(StepVerifier::create)
                .expectNext("one", "two")
                .verifyComplete();
        verifyNoInteractions(presentation);
    }

    @Test
    void inheritedReadOperationsRemainAvailable() {
        var options = new ListOptions();
        var request = PageRequestImpl.of(1, 10);
        when(sources.listNamesBy(Tag.class, options, request))
                .thenReturn(Mono.just(new ListResult<>(1, 10, 1, List.of("tag"))));
        client.listNamesBy(Tag.class, options, request)
                .as(StepVerifier::create)
                .assertNext(result -> assertThat(result.getItems()).containsExactly("tag"))
                .verifyComplete();

        var source = new Unstructured();
        var translated = new Unstructured();
        var gvk = Tag.GVK;
        when(sources.fetch(gvk, "tag")).thenReturn(Mono.just(source));
        when(presentation.presentAll(List.of(source))).thenReturn(Mono.just(List.of(translated)));
        client.fetch(gvk, "tag")
                .contextWrite(Context.of(LocalizationContext.class, publicContext))
                .as(StepVerifier::create)
                .expectNext(translated)
                .verifyComplete();
    }
}
