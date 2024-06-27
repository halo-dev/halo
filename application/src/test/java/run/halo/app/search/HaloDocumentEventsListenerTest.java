package run.halo.app.search;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
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

    @Test
    void shouldRebuildIndicesWhenReceivingRebuildRequestEvent() {
        listener.setBufferSize(1);
        var searchEngine = mock(SearchEngine.class);
        when(searchEngine.available()).thenReturn(true);
        when(extensionGetter.getEnabledExtension(SearchEngine.class))
            .thenReturn(Mono.just(searchEngine));
        var docsProvider = mock(HaloDocumentsProvider.class);

        var docs = List.of(new HaloDocument(), new HaloDocument(), new HaloDocument());

        when(docsProvider.fetchAll()).thenReturn(Flux.fromIterable(docs));
        when(extensionGetter.getExtensions(HaloDocumentsProvider.class))
            .thenReturn(Flux.just(docsProvider));
        listener.onApplicationEvent(new HaloDocumentRebuildRequestEvent(this));
        verify(searchEngine, times(3)).addOrUpdate(any());
    }

    @Test
    void shouldAddDocsWhenReceivingAddRequestEvent() {
        var searchEngine = mock(SearchEngine.class);
        when(searchEngine.available()).thenReturn(true);
        when(extensionGetter.getEnabledExtension(SearchEngine.class))
            .thenReturn(Mono.just(searchEngine));
        var docs = List.of(new HaloDocument());
        listener.onApplicationEvent(new HaloDocumentAddRequestEvent(this, docs));
        verify(searchEngine).addOrUpdate(docs);
    }

    @Test
    void shouldDeleteDocsWhenReceivingDeleteRequestEvent() {
        var searchEngine = mock(SearchEngine.class);
        when(searchEngine.available()).thenReturn(true);
        when(extensionGetter.getEnabledExtension(SearchEngine.class))
            .thenReturn(Mono.just(searchEngine));
        var docIds = List.of("1", "2", "3");
        listener.onApplicationEvent(new HaloDocumentDeleteRequestEvent(this, docIds));
        verify(searchEngine).deleteDocument(docIds);
    }

    @Test
    void shouldFailWhenSearchEngineIsUnavailable() {
        var searchEngine = mock(SearchEngine.class);
        when(searchEngine.available()).thenReturn(false);
        when(extensionGetter.getEnabledExtension(SearchEngine.class))
            .thenReturn(Mono.just(searchEngine));
        assertThrows(
            SearchEngineUnavailableException.class,
            () -> listener.onApplicationEvent(new HaloDocumentRebuildRequestEvent(this))
        );
    }

}