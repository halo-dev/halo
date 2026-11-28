package run.halo.app.localization;

import java.util.List;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.extension.Extension;

/**
 * Shared processing boundary used inside public read clients. Callers supply already authorized sources; this service
 * isolates display values and never writes them back. It reads language and consumer scope at subscription time.
 *
 * @since 2.28.0
 */
public interface ResourcePresentationService {
    /** Presents one authorized Extension snapshot in the current consumer context. */
    default <T extends Extension> Mono<T> present(T source) {
        return presentAll(List.of(source)).map(List::getFirst);
    }

    /** Presents authorized Extension snapshots in bounded batches, preserving order and cardinality. */
    <T extends Extension> Mono<List<T>> presentAll(List<T> sources);

    /** Presents an authorized Extension stream in bounded batches, preserving order and cardinality. */
    <T extends Extension> Flux<T> presentAll(Flux<T> sources);

    /** Presents authorized detached DTOs using their owner's declaration and stable identities. */
    <T> Mono<List<T>> presentAll(List<T> sources, LocalizableResourceDescriptor<T> descriptor);

    /** Presents a derived public URL without changing its source value on failure. */
    Mono<String> presentUrl(String sourceUrl);

    /** Creates a read-only domain client; no per-call processing step is needed in a Finder or public endpoint. */
    default <T> ReactivePresentationResourceClient<T> client(LocalizableResourceSource<T> source) {
        return new ReactivePresentationResourceClient<>(source, this);
    }
}
