package run.halo.app.localization;

import java.util.List;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.extension.Extension;

/**
 * Context-bound projection helpers for public read boundaries, never for persistence clients.
 *
 * @since 2.28.0
 */
public final class PublicLocalization {
    private PublicLocalization() {}

    /**
     * Projects a resource using the current subscription context.
     *
     * @param <T> resource type
     * @param source original resource
     * @param service resource projection service
     * @return a display projection, or the source when no context is present
     */
    public static <T extends Extension> Mono<T> project(T source, ResourceLocalizationService service) {
        return LocalizationContext.current()
                .flatMap(context -> service.localize(source, context))
                .defaultIfEmpty(source);
    }

    /**
     * Projects a list while preserving its order and size.
     *
     * @param <T> resource type
     * @param sources original resources
     * @param service resource projection service
     * @return display projections in the original order
     */
    public static <T extends Extension> Mono<List<T>> project(List<T> sources, ResourceLocalizationService service) {
        return LocalizationContext.current()
                .flatMap(context -> service.localizeAll(sources, context))
                .defaultIfEmpty(sources);
    }

    /**
     * Projects a resource after its publisher resolves.
     *
     * @param <T> resource type
     * @param source resource publisher
     * @param service resource projection service
     * @return a publisher retaining the original empty-result behavior
     */
    public static <T extends Extension> Mono<T> project(Mono<T> source, ResourceLocalizationService service) {
        return source.flatMap(item -> project(item, service));
    }

    /**
     * Projects a resource stream in bounded, ordered batches.
     *
     * @param <T> resource type
     * @param sources resource publisher
     * @param service resource projection service
     * @return display projections without collecting the entire stream
     */
    public static <T extends Extension> Flux<T> project(Flux<T> sources, ResourceLocalizationService service) {
        return Flux.deferContextual(view -> {
            var context = view.<LocalizationContext>getOrEmpty(LocalizationContext.class)
                    .filter(LocalizationContext::enabled);
            if (context.isEmpty()) {
                return sources;
            }
            return sources.buffer(100)
                    .concatMap(
                            batch -> service.localizeAll(batch, context.get()).defaultIfEmpty(batch))
                    .flatMapIterable(items -> items);
        });
    }
}
