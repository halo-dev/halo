package run.halo.app.search;

import java.time.Duration;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;
import run.halo.app.search.event.HaloDocumentAddRequestEvent;
import run.halo.app.search.event.HaloDocumentDeleteRequestEvent;
import run.halo.app.search.event.HaloDocumentRebuildRequestEvent;

@Component
public class HaloDocumentEventsListener {

    private final ExtensionGetter extensionGetter;

    private int bufferSize;

    public HaloDocumentEventsListener(ExtensionGetter extensionGetter) {
        this.extensionGetter = extensionGetter;
        this.bufferSize = 200;
    }

    /**
     * Only for testing.
     *
     * @param bufferSize new buffer size for rebuilding indices
     */
    void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    @EventListener
    @Async
    void onApplicationEvent(HaloDocumentRebuildRequestEvent event) {
        getSearchEngine()
            .doOnNext(SearchEngine::deleteAll)
            .flatMap(searchEngine -> extensionGetter.getExtensions(HaloDocumentsProvider.class)
                .flatMap(HaloDocumentsProvider::fetchAll)
                .buffer(this.bufferSize)
                .doOnNext(searchEngine::addOrUpdate)
                .then())
            .blockOptional(Duration.ofMinutes(1));
    }

    @EventListener
    @Async
    void onApplicationEvent(HaloDocumentAddRequestEvent event) {
        getSearchEngine()
            .doOnNext(searchEngine -> searchEngine.addOrUpdate(event.getDocuments()))
            .then()
            .blockOptional(Duration.ofMinutes(1));
    }

    @EventListener
    @Async
    void onApplicationEvent(HaloDocumentDeleteRequestEvent event) {
        getSearchEngine()
            .doOnNext(searchEngine -> searchEngine.deleteDocument(event.getDocIds()))
            .then()
            .blockOptional(Duration.ofMinutes(1));
    }

    private Mono<SearchEngine> getSearchEngine() {
        return extensionGetter.getEnabledExtension(SearchEngine.class)
            .filter(SearchEngine::available)
            .switchIfEmpty(Mono.error(SearchEngineUnavailableException::new));
    }
}
