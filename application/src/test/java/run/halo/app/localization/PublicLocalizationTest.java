package run.halo.app.localization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.context.Context;
import run.halo.app.core.extension.content.Tag;

class PublicLocalizationTest {
    private final ResourceLocalizationService service = mock(ResourceLocalizationService.class);

    @Test
    void unlocalizedFirstItemDoesNotReadTheNextBatch() {
        var emitted = new AtomicInteger();
        var tag = new Tag();
        var sources = Flux.range(0, 150).map(index -> tag).doOnNext(ignored -> emitted.incrementAndGet());

        PublicLocalization.project(sources, service)
                .next()
                .as(StepVerifier::create)
                .expectNext(tag)
                .verifyComplete();

        assertThat(emitted).hasValue(1);
        verifyNoInteractions(service);
    }

    @Test
    void unlocalizedFirstItemDoesNotWaitForCompletionOrLaterErrors() {
        var tag = new Tag();
        for (var remainder : List.of(Flux.<Tag>never(), Flux.<Tag>error(new IllegalStateException("later")))) {
            PublicLocalization.project(Flux.just(tag).concatWith(remainder), service)
                    .next()
                    .as(StepVerifier::create)
                    .expectNext(tag)
                    .expectComplete()
                    .verify(Duration.ofSeconds(1));
        }
        verifyNoInteractions(service);
    }

    @Test
    void sourceConsoleAndInternalContextsKeepTheOriginalStream() {
        var tag = new Tag();
        var contexts = List.of(
                new LocalizationContext(Locale.ENGLISH, Locale.ENGLISH, List.of(), LocalizationScope.PUBLIC_PAGE),
                new LocalizationContext(Locale.CHINA, Locale.ENGLISH, List.of(), LocalizationScope.CONSOLE),
                new LocalizationContext(Locale.CHINA, Locale.ENGLISH, List.of(), LocalizationScope.INTERNAL));
        for (var context : contexts) {
            PublicLocalization.project(Flux.just(tag).concatWith(Flux.never()), service)
                    .next()
                    .contextWrite(Context.of(LocalizationContext.class, context))
                    .as(StepVerifier::create)
                    .expectNext(tag)
                    .expectComplete()
                    .verify(Duration.ofSeconds(1));
        }
        verifyNoInteractions(service);
    }

    @Test
    void enabledContextProjectsOrderedBatchesAndDoesNotLeakToAnotherSubscriber() {
        var context = new LocalizationContext(Locale.CHINA, Locale.ENGLISH, List.of(), LocalizationScope.PUBLIC_PAGE);
        var source = new Tag();
        var translated = new Tag();
        var batchSizes = new ArrayList<Integer>();
        when(service.localizeAll(any(), any())).thenAnswer(invocation -> {
            List<Tag> batch = invocation.getArgument(0);
            assertThat((LocalizationContext) invocation.getArgument(1)).isEqualTo(context);
            batchSizes.add(batch.size());
            return Mono.just(batch.stream().map(ignored -> translated).toList());
        });
        var projected = PublicLocalization.project(Flux.range(0, 205).map(index -> source), service);

        assertThat(projected
                        .contextWrite(Context.of(LocalizationContext.class, context))
                        .collectList()
                        .block())
                .hasSize(205)
                .containsOnly(translated);
        assertThat(batchSizes).containsExactly(100, 100, 5);
        assertThat(projected.next().block()).isSameAs(source);
        assertThat(batchSizes).containsExactly(100, 100, 5);
    }
}
