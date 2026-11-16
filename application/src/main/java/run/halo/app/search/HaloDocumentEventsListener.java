package run.halo.app.search;

import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;
import reactor.util.retry.Retry;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;
import run.halo.app.search.event.HaloDocumentAddRequestEvent;
import run.halo.app.search.event.HaloDocumentDeleteRequestEvent;
import run.halo.app.search.event.HaloDocumentRebuildRequestEvent;

@Component
@Slf4j
public class HaloDocumentEventsListener {

    private static final int MAX_VARIANTS = 100;

    private static final Duration INDEX_IDLE_TIMEOUT = Duration.ofMinutes(1);

    private final ExtensionGetter extensionGetter;

    private final SearchIndexWriter indexWriter = new SearchIndexWriter(INDEX_IDLE_TIMEOUT);

    private final ExecutorService worker = Executors.newSingleThreadExecutor(Thread.ofVirtual()
            .name("search-index")
            .inheritInheritableThreadLocals(false)
            .factory());

    private final Deque<Supplier<Mono<Void>>> requests = new ArrayDeque<>();

    private boolean draining;

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
    void onApplicationEvent(HaloDocumentRebuildRequestEvent event) {
        boolean localizedOnly = event.getSource() instanceof HaloDocumentVariantProvider;
        enqueue(() -> rebuild(localizedOnly));
    }

    private Mono<Void> rebuild(boolean localizedOnly) {
        return getSearchEngine()
                .filter(engine -> !localizedOnly || engine.supportsLocalizedDocuments())
                .flatMap(engine -> indexWrite(engine::deleteAll)
                        .then(indexDocuments(
                                Flux.defer(() -> extensionGetter
                                        .getExtensions(HaloDocumentsProvider.class)
                                        .flatMap(HaloDocumentsProvider::fetchAll)),
                                engine)));
    }

    @EventListener
    void onApplicationEvent(HaloDocumentAddRequestEvent event) {
        enqueue(() ->
                getSearchEngine().flatMap(engine -> indexDocuments(Flux.fromIterable(event.getDocuments()), engine)));
    }

    @EventListener
    void onApplicationEvent(HaloDocumentDeleteRequestEvent event) {
        deleteDocuments(event.getDocIds());
    }

    public void deleteDocuments(Iterable<String> ids) {
        enqueue(() -> getSearchEngine().flatMap(engine -> indexWrite(() -> engine.deleteDocument(ids))));
    }

    public void refreshDocument(String id, Supplier<Mono<HaloDocument>> loadCurrentDocument) {
        enqueue(() -> refresh(id, loadCurrentDocument));
    }

    private Mono<Void> refresh(String id, Supplier<Mono<HaloDocument>> loadCurrentDocument) {
        return getSearchEngine()
                .flatMap(engine -> Mono.defer(loadCurrentDocument)
                        .timeout(INDEX_IDLE_TIMEOUT)
                        .map(Optional::of)
                        .defaultIfEmpty(Optional.empty())
                        .flatMap(document -> {
                            if (document.isEmpty()) {
                                return indexWrite(() -> engine.deleteDocument(List.of(id)));
                            }
                            return indexDocuments(Mono.just(document.get()).flux(), engine);
                        }));
    }

    private synchronized void enqueue(Supplier<Mono<Void>> request) {
        if (worker.isShutdown()) {
            return;
        }
        requests.addLast(request);
        if (draining) {
            return;
        }
        draining = true;
        worker.execute(this::drain);
    }

    private void drain() {
        Supplier<Mono<Void>> request;
        while ((request = nextRequest()) != null) {
            try {
                request.get().block();
            } catch (RuntimeException error) {
                if (!worker.isShutdown()) {
                    log.warn("Search index request failed", error);
                }
            }
        }
    }

    private synchronized Supplier<Mono<Void>> nextRequest() {
        var request = requests.pollFirst();
        if (request == null) {
            draining = false;
        }
        return request;
    }

    @PreDestroy
    synchronized void shutdown() {
        worker.shutdownNow();
        indexWriter.close();
        requests.clear();
    }

    private Mono<Void> indexWrite(Runnable operation) {
        return indexWriter.write(operation);
    }

    Mono<Void> indexDocuments(Flux<HaloDocument> documents, SearchEngine engine) {
        return documents
                .concatMap(document -> expand(document, engine).collectList())
                .timeout(INDEX_IDLE_TIMEOUT)
                // Flush completed groups before propagating an upstream failure or idle timeout.
                .materialize()
                .buffer(bufferSize)
                .concatMap(signals -> writeBatch(signals, engine))
                .then();
    }

    private Mono<Void> writeBatch(List<Signal<List<HaloDocument>>> signals, SearchEngine engine) {
        var documents = new ArrayList<HaloDocument>();
        Throwable failure = null;
        for (var signal : signals) {
            if (signal.isOnNext()) {
                documents.addAll(Objects.requireNonNull(signal.get()));
            } else if (signal.isOnError()) {
                failure = signal.getThrowable();
            }
        }
        Mono<Void> write = Mono.empty();
        if (!documents.isEmpty()) {
            write = indexWrite(() -> engine.addOrUpdate(documents));
        }
        return failure == null ? write : write.then(Mono.error(failure));
    }

    private Flux<HaloDocument> expand(HaloDocument source, SearchEngine engine) {
        if (!engine.supportsLocalizedDocuments()) {
            return Flux.just(source);
        }
        if (source.getLocale() != null) {
            return Flux.just(source);
        }
        return Mono.defer(() -> extensionGetter.getEnabledExtension(HaloDocumentVariantProvider.class))
                .flatMapMany(provider -> provider.variants(source))
                .take(MAX_VARIANTS + 1L)
                .collectList()
                .timeout(Duration.ofSeconds(5))
                .retryWhen(Retry.backoff(2, Duration.ofMillis(250))
                        .jitter(0)
                        .doBeforeRetry(retry -> log.debug(
                                "Retrying search variants for document {} (retry {})",
                                source.getId(),
                                retry.totalRetries() + 1,
                                retry.failure()))
                        .onRetryExhaustedThrow((spec, retry) -> retry.failure()))
                .map(variants -> {
                    if (variants.size() > MAX_VARIANTS) {
                        throw new IllegalArgumentException("Too many document variants");
                    }
                    var locales = new HashSet<String>();
                    var documents = new ArrayList<HaloDocument>();
                    documents.add(source);
                    for (var variant : variants) {
                        String locale = new Locale.Builder()
                                .setLanguageTag(variant.locale())
                                .build()
                                .toLanguageTag();
                        if (locale.equals("und")) {
                            throw new IllegalArgumentException("Undetermined locale");
                        }
                        if (!locales.add(locale)) {
                            throw new IllegalArgumentException("Duplicate locale");
                        }
                        var copy = new HaloDocument();
                        BeanUtils.copyProperties(source, copy);
                        copy.setId(source.getId() + ":locale:" + locale);
                        copy.setSourceId(source.getId());
                        copy.setLocale(locale);
                        copy.setTitle(Objects.requireNonNull(variant.title()));
                        copy.setDescription(variant.description());
                        copy.setContent(Objects.requireNonNull(variant.content()));
                        copy.setPermalink(Objects.requireNonNull(variant.permalink()));
                        documents.add(copy);
                    }
                    return documents;
                })
                .onErrorResume(error -> {
                    log.warn(
                            "Search variants unavailable for document {}; indexing source only", source.getId(), error);
                    return Mono.just(new ArrayList<>(List.of(source)));
                })
                .flatMapMany(Flux::fromIterable);
    }

    private Mono<SearchEngine> getSearchEngine() {
        return extensionGetter
                .getEnabledExtension(SearchEngine.class)
                .timeout(INDEX_IDLE_TIMEOUT)
                .filter(SearchEngine::available)
                .switchIfEmpty(Mono.error(SearchEngineUnavailableException::new));
    }
}
