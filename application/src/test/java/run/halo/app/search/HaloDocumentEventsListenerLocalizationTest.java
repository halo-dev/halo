package run.halo.app.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;
import run.halo.app.search.event.HaloDocumentAddRequestEvent;

@ExtendWith(MockitoExtension.class)
class HaloDocumentEventsListenerLocalizationTest {

    @Mock
    ExtensionGetter extensionGetter;

    @Mock
    SearchEngine searchEngine;

    @InjectMocks
    HaloDocumentEventsListener listener;

    @Captor
    ArgumentCaptor<Iterable<HaloDocument>> documentsCaptor;

    @AfterEach
    void close() {
        listener.shutdown();
    }

    @ParameterizedTest
    @ValueSource(ints = {11, 99, 100})
    void shouldIndexEverySupportedTargetLanguage(int targetCount) {
        enableEngine(true);
        var source = document("source");
        var locales = Arrays.stream(Locale.getISOLanguages())
                .map(Locale::forLanguageTag)
                .map(Locale::toLanguageTag)
                .filter(locale -> !locale.equals("zh"))
                .distinct()
                .limit(targetCount)
                .toList();
        assertThat(locales).hasSize(targetCount);
        variantProvider(original -> Flux.fromIterable(locales).map(locale -> variant(original, locale)));

        listener.onApplicationEvent(new HaloDocumentAddRequestEvent(this, List.of(source)));

        var expectedIds = Stream.concat(
                        Stream.of(source.getId()), locales.stream().map(locale -> source.getId() + ":locale:" + locale))
                .toList();
        assertThat(indexedDocuments()).extracting(HaloDocument::getId).containsExactlyElementsOf(expectedIds);
    }

    @Test
    void shouldKeepEachSourceAndItsVariantsInOneBatch() {
        enableEngine(true);
        listener.setBufferSize(1);
        var first = document("first");
        var second = document("second");
        variantProvider(source -> Flux.just(variant(source, "en"), variant(source, "fr")));

        listener.onApplicationEvent(new HaloDocumentAddRequestEvent(this, List.of(first, second)));

        verify(searchEngine, timeout(3000).times(2)).addOrUpdate(documentsCaptor.capture());
        var batches = documentsCaptor.getAllValues();
        assertThat(documents(batches.getFirst()))
                .extracting(HaloDocument::getId)
                .containsExactly("first", "first:locale:en", "first:locale:fr");
        assertThat(documents(batches.getLast()))
                .extracting(HaloDocument::getId)
                .containsExactly("second", "second:locale:en", "second:locale:fr");
    }

    @Test
    void shouldKeepLegacyEnginesSourceOnly() {
        enableEngine(false);
        var source = document("source");

        listener.onApplicationEvent(new HaloDocumentAddRequestEvent(this, List.of(source)));

        assertThat(indexedDocuments()).containsExactly(source);
        verify(extensionGetter, never()).getEnabledExtension(HaloDocumentVariantProvider.class);
    }

    @Test
    void shouldIndexOriginalWhenNoVariantProviderIsEnabled() {
        enableEngine(true);
        when(extensionGetter.getEnabledExtension(HaloDocumentVariantProvider.class))
                .thenReturn(Mono.empty());
        var source = document("source");

        listener.onApplicationEvent(new HaloDocumentAddRequestEvent(this, List.of(source)));

        assertThat(indexedDocuments()).containsExactly(source);
    }

    @Test
    void shouldIndexOriginalWhenVariantProviderFails() {
        enableEngine(true);
        variantProvider(source -> Flux.error(new IllegalStateException("Translation provider unavailable")));
        var source = document("source");

        listener.onApplicationEvent(new HaloDocumentAddRequestEvent(this, List.of(source)));

        assertThat(indexedDocuments()).containsExactly(source);
    }

    @Test
    void shouldAllowSourceFallbackToMakeProgressForMoreThanOneMinute() {
        when(searchEngine.supportsLocalizedDocuments()).thenReturn(true);
        variantProvider(source -> Flux.never());
        var sources = IntStream.range(0, 13)
                .mapToObj(index -> document("source-" + index))
                .toList();

        StepVerifier.withVirtualTime(() -> listener.indexDocuments(Flux.fromIterable(sources), searchEngine))
                .thenAwait(Duration.ofSeconds(205))
                .expectComplete()
                .verify(Duration.ofSeconds(5));

        assertThat(indexedDocuments()).containsExactlyElementsOf(sources);
    }

    @Test
    void shouldFlushCompletedGroupsBeforeReportingAnIdleSourceProvider() {
        when(searchEngine.supportsLocalizedDocuments()).thenReturn(true);
        variantProvider(source -> Flux.empty());
        var source = document("source");

        StepVerifier.withVirtualTime(
                        () -> listener.indexDocuments(Flux.concat(Flux.just(source), Flux.never()), searchEngine))
                .thenAwait(Duration.ofMinutes(1))
                .expectError(TimeoutException.class)
                .verify(Duration.ofSeconds(5));

        assertThat(indexedDocuments()).containsExactly(source);
    }

    @Test
    void shouldFlushCompletedGroupsBeforePropagatingASourceProviderFailure() {
        when(searchEngine.supportsLocalizedDocuments()).thenReturn(true);
        variantProvider(source -> Flux.empty());
        var source = document("source");
        var failure = new IllegalStateException("Source provider unavailable");

        StepVerifier.create(listener.indexDocuments(Flux.concat(Flux.just(source), Flux.error(failure)), searchEngine))
                .expectErrorSatisfies(error -> assertThat(error).isSameAs(failure))
                .verify(Duration.ofSeconds(5));

        assertThat(indexedDocuments()).containsExactly(source);
    }

    @Test
    void shouldPropagateSearchEngineWriteFailures() {
        when(searchEngine.supportsLocalizedDocuments()).thenReturn(true);
        variantProvider(source -> Flux.empty());
        var failure = new IllegalStateException("Index write failed");
        doThrow(failure).when(searchEngine).addOrUpdate(any());

        StepVerifier.create(listener.indexDocuments(Flux.just(document("source")), searchEngine))
                .expectErrorSatisfies(error -> assertThat(error).isSameAs(failure))
                .verify(Duration.ofSeconds(5));
    }

    private void enableEngine(boolean localized) {
        when(searchEngine.available()).thenReturn(true);
        when(searchEngine.supportsLocalizedDocuments()).thenReturn(localized);
        when(extensionGetter.getEnabledExtension(SearchEngine.class)).thenReturn(Mono.just(searchEngine));
    }

    private void variantProvider(HaloDocumentVariantProvider provider) {
        when(extensionGetter.getEnabledExtension(HaloDocumentVariantProvider.class))
                .thenReturn(Mono.just(provider));
    }

    private List<HaloDocument> indexedDocuments() {
        verify(searchEngine, timeout(3000)).addOrUpdate(documentsCaptor.capture());
        return documents(documentsCaptor.getValue());
    }

    private static List<HaloDocument> documents(Iterable<HaloDocument> documents) {
        var result = new ArrayList<HaloDocument>();
        documents.forEach(result::add);
        return result;
    }

    private static HaloDocumentVariantProvider.Variant variant(HaloDocument source, String locale) {
        return new HaloDocumentVariantProvider.Variant(
                locale,
                "Translated title " + locale,
                "Translated description " + locale,
                "Translated content " + locale,
                "/" + locale + source.getPermalink());
    }

    private static HaloDocument document(String id) {
        var document = new HaloDocument();
        document.setId(id);
        document.setMetadataName(id);
        document.setType("post.content.halo.run");
        document.setOwnerName("admin");
        document.setTitle("Original title");
        document.setContent("Original body");
        document.setPermalink("/archives/" + id);
        document.setCreationTimestamp(Instant.parse("2026-01-01T00:00:00Z"));
        document.setPublished(true);
        document.setExposed(true);
        return document;
    }
}
