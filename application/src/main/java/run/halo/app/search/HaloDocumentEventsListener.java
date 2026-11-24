package run.halo.app.search;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;
import run.halo.app.search.event.HaloDocumentAddRequestEvent;
import run.halo.app.search.event.HaloDocumentDeleteRequestEvent;
import run.halo.app.search.event.HaloDocumentRebuildRequestEvent;

@Component
@Slf4j
public class HaloDocumentEventsListener {

    private static final int MAX_VARIANTS = 100;

    private static final Duration VARIANT_RESOLUTION_TIMEOUT = Duration.ofSeconds(5);

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
        boolean localizedOnly = event.getSource() instanceof HaloDocumentVariantProvider;
        getSearchEngine()
                .filter(searchEngine -> !localizedOnly || searchEngine.supportsLocalizedDocuments())
                .doOnNext(SearchEngine::deleteAll)
                .flatMap(searchEngine -> indexDocuments(
                        extensionGetter
                                .getExtensions(HaloDocumentsProvider.class)
                                .flatMap(HaloDocumentsProvider::fetchAll),
                        searchEngine))
                .blockOptional(Duration.ofMinutes(1));
    }

    @EventListener
    @Async
    void onApplicationEvent(HaloDocumentAddRequestEvent event) {
        getSearchEngine()
                .flatMap(searchEngine -> {
                    if (!searchEngine.supportsLocalizedDocuments()) {
                        return Mono.fromRunnable(() -> searchEngine.addOrUpdate(event.getDocuments()));
                    }
                    return indexDocuments(Flux.fromIterable(event.getDocuments()), searchEngine);
                })
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

    private Mono<Void> indexDocuments(Flux<HaloDocument> documents, SearchEngine searchEngine) {
        return documents
                .concatMap(document -> expand(document, searchEngine).collectList())
                .buffer(this.bufferSize)
                .map(batch -> batch.stream().flatMap(List::stream).toList())
                .doOnNext(searchEngine::addOrUpdate)
                .then();
    }

    private Flux<HaloDocument> expand(HaloDocument source, SearchEngine searchEngine) {
        if (!searchEngine.supportsLocalizedDocuments()) {
            return Flux.just(source);
        }
        if (source.getLocale() != null) {
            return Flux.just(source);
        }
        return extensionGetter
                .getEnabledExtension(HaloDocumentVariantProvider.class)
                .flatMapMany(provider -> provider.variants(source))
                .take(MAX_VARIANTS + 1L)
                .collectList()
                .timeout(VARIANT_RESOLUTION_TIMEOUT)
                .map(variants -> expand(source, variants))
                .onErrorResume(error -> {
                    log.warn(
                            "Search variants unavailable for document {}; indexing source only", source.getId(), error);
                    return Mono.just(List.of(source));
                })
                .flatMapMany(Flux::fromIterable);
    }

    private static List<HaloDocument> expand(HaloDocument source, List<HaloDocumentVariantProvider.Variant> variants) {
        if (variants.size() > MAX_VARIANTS) {
            throw new IllegalArgumentException("Too many document variants");
        }
        var locales = new HashSet<String>();
        var documents = new ArrayList<HaloDocument>(variants.size() + 1);
        documents.add(source);
        for (var variant : variants) {
            String locale = new Locale.Builder()
                    .setLanguageTag(Objects.requireNonNull(variant.locale()))
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
    }

    private Mono<SearchEngine> getSearchEngine() {
        return extensionGetter
                .getEnabledExtension(SearchEngine.class)
                .filter(SearchEngine::available)
                .switchIfEmpty(Mono.error(SearchEngineUnavailableException::new));
    }
}
