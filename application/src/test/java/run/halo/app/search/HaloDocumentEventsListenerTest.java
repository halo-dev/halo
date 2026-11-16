package run.halo.app.search;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;
import run.halo.app.search.event.HaloDocumentAddRequestEvent;
import run.halo.app.search.event.HaloDocumentDeleteRequestEvent;
import run.halo.app.search.event.HaloDocumentRebuildRequestEvent;

@ExtendWith(MockitoExtension.class)
class HaloDocumentEventsListenerTest {

    @Mock
    ExtensionGetter extensionGetter;

    @InjectMocks
    HaloDocumentEventsListener listener;

    @AfterEach
    void close() {
        listener.shutdown();
    }

    @Test
    void shouldRebuildIndicesWhenReceivingRebuildRequestEvent() {
        listener.setBufferSize(1);
        var searchEngine = mock(SearchEngine.class);
        when(searchEngine.available()).thenReturn(true);
        when(extensionGetter.getEnabledExtension(SearchEngine.class)).thenReturn(Mono.just(searchEngine));
        var docsProvider = mock(HaloDocumentsProvider.class);

        var docs = List.of(new HaloDocument(), new HaloDocument(), new HaloDocument());

        when(docsProvider.fetchAll()).thenReturn(Flux.fromIterable(docs));
        when(extensionGetter.getExtensions(HaloDocumentsProvider.class)).thenReturn(Flux.just(docsProvider));
        listener.onApplicationEvent(new HaloDocumentRebuildRequestEvent(this));
        verify(searchEngine, timeout(3000).times(3)).addOrUpdate(any());
    }

    @Test
    void shouldAddDocsWhenReceivingAddRequestEvent() {
        var searchEngine = mock(SearchEngine.class);
        when(searchEngine.available()).thenReturn(true);
        when(extensionGetter.getEnabledExtension(SearchEngine.class)).thenReturn(Mono.just(searchEngine));
        var docs = List.of(new HaloDocument());
        listener.onApplicationEvent(new HaloDocumentAddRequestEvent(this, docs));
        verify(searchEngine, timeout(3000)).addOrUpdate(docs);
    }

    @Test
    void shouldDeleteDocsWhenReceivingDeleteRequestEvent() {
        var searchEngine = mock(SearchEngine.class);
        when(searchEngine.available()).thenReturn(true);
        when(extensionGetter.getEnabledExtension(SearchEngine.class)).thenReturn(Mono.just(searchEngine));
        var docIds = List.of("1", "2", "3");
        listener.onApplicationEvent(new HaloDocumentDeleteRequestEvent(this, docIds));
        verify(searchEngine, timeout(3000)).deleteDocument(docIds);
    }

    @Test
    void shouldContinueAfterSearchEngineIsUnavailable() {
        var searchEngine = mock(SearchEngine.class);
        when(searchEngine.available()).thenReturn(false, true);
        when(extensionGetter.getEnabledExtension(SearchEngine.class)).thenReturn(Mono.just(searchEngine));
        listener.onApplicationEvent(new HaloDocumentRebuildRequestEvent(this));
        listener.onApplicationEvent(new HaloDocumentDeleteRequestEvent(this, List.of("post")));
        verify(searchEngine, timeout(3000)).deleteDocument(List.of("post"));
        verify(searchEngine, never()).deleteAll();
    }

    @Test
    void variantProviderRebuildShouldNotClearLegacyIndices() {
        var searchEngine = mock(SearchEngine.class);
        when(searchEngine.available()).thenReturn(true);
        when(extensionGetter.getEnabledExtension(SearchEngine.class)).thenReturn(Mono.just(searchEngine));
        var provider = mock(HaloDocumentVariantProvider.class);
        listener.onApplicationEvent(new HaloDocumentRebuildRequestEvent(provider));
        listener.onApplicationEvent(new HaloDocumentDeleteRequestEvent(this, List.of("post")));
        verify(searchEngine, timeout(3000)).deleteDocument(List.of("post"));
        verify(searchEngine, never()).deleteAll();
    }
}
