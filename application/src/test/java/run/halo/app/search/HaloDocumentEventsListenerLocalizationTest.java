package run.halo.app.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Post;
import run.halo.app.localization.ResourceReference;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;
import run.halo.app.search.event.HaloDocumentAddRequestEvent;
import run.halo.app.search.event.HaloDocumentRebuildRequestEvent;

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

    @Test
    void shouldExpandOriginalIntoLanguageVariants() {
        enableEngine(true);
        var source = document("source");
        variantProvider(original -> Flux.just(variant(original, "en"), variant(original, "zh-hant")));

        listener.onApplicationEvent(new HaloDocumentAddRequestEvent(this, List.of(source)));

        var documents = indexedDocuments();
        assertThat(documents)
                .extracting(HaloDocument::getId)
                .containsExactly("source", "source:locale:en", "source:locale:zh-Hant");
        assertThat(documents.get(1).getSourceId()).isEqualTo("source");
        assertThat(documents.get(1).getLocale()).isEqualTo("en");
        assertThat(documents.get(2).getLocale()).isEqualTo("zh-Hant");
        assertThat(documents)
                .allSatisfy(document ->
                        assertThat(document.getResource()).isEqualTo(new ResourceReference(Post.GVK, "source")));
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
    void shouldIndexOriginalWhenVariantProviderFails() {
        enableEngine(true);
        var source = document("source");
        variantProvider(ignored -> Flux.error(new IllegalStateException("Translation provider unavailable")));

        listener.onApplicationEvent(new HaloDocumentAddRequestEvent(this, List.of(source)));

        assertThat(indexedDocuments()).containsExactly(source);
    }

    @Test
    void shouldRejectDuplicateLocalesAndIndexOriginal() {
        enableEngine(true);
        var source = document("source");
        variantProvider(original -> Flux.just(variant(original, "en"), variant(original, "EN")));

        listener.onApplicationEvent(new HaloDocumentAddRequestEvent(this, List.of(source)));

        assertThat(indexedDocuments()).containsExactly(source);
    }

    @Test
    void variantProviderRebuildShouldNotClearLegacyIndices() {
        enableEngine(false);
        var provider = mock(HaloDocumentVariantProvider.class);

        listener.onApplicationEvent(new HaloDocumentRebuildRequestEvent(provider));

        verify(searchEngine, never()).deleteAll();
        verify(searchEngine, never()).addOrUpdate(any());
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
        verify(searchEngine).addOrUpdate(documentsCaptor.capture());
        var documents = new ArrayList<HaloDocument>();
        documentsCaptor.getValue().forEach(documents::add);
        return documents;
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
        document.setResource(new ResourceReference(Post.GVK, id));
        document.setOwnerName("admin");
        document.setTitle("Original title");
        document.setContent("Original body");
        document.setPermalink("/archives/" + id);
        document.setCreationTimestamp(Instant.EPOCH);
        document.setPublished(true);
        document.setExposed(true);
        return document;
    }
}
