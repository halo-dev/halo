package run.halo.app.core.attachment;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;
import run.halo.app.core.attachment.extension.LocalThumbnail;
import run.halo.app.core.attachment.extension.Thumbnail;
import run.halo.app.extension.Extension;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.ReactiveExtensionPaginatedOperator;

/**
 * <p>TODO Remove this class in the next major version.</p>
 * when this class is removed, the following code should be added:
 * <pre>
 * <code>
 * schemeManager.register(LocalThumbnail.class, indexSpec -> {
 *       indexSpec.add(new IndexSpec()
 *           // mark the index as unique
 *           .setUnique(true)
 *           .setName(LocalThumbnail.UNIQUE_IMAGE_AND_SIZE_INDEX)
 *           .setIndexFunc(simpleAttribute(LocalThumbnail.class,
 *               LocalThumbnail::uniqueImageAndSize)
 *           )
 *       );
 *       // ...
 *  });
 *  schemeManager.register(Thumbnail.class, indexSpec -> {
 *       indexSpec.add(new IndexSpec()
 *            // mark the index as unique
 *           .setUnique(true)
 *           .setName(Thumbnail.ID_INDEX)
 *           .setIndexFunc(simpleAttribute(Thumbnail.class, Thumbnail::idIndexFunc))
 *       );
 *       // ...
 *  });
 * </code>
 * </pre>
 *
 * @see run.halo.app.infra.SchemeInitializer
 * @since 2.20.9
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ThumbnailMigration {
    private final LocalThumbnailService localThumbnailService;
    private final ReactiveExtensionClient client;
    private final ReactiveExtensionPaginatedOperator extensionPaginatedOperator;

    @Async
    @EventListener(ApplicationStartedEvent.class)
    public void onApplicationEvent(@NonNull ApplicationStartedEvent event) {
        cleanupThumbnail(Thumbnail.class,
            thumbnail -> new UniqueKey(thumbnail.getSpec().getImageUri(),
                thumbnail.getSpec().getSize().name()))
            .count()
            .doOnNext(count -> log.info("Deleted {} duplicate thumbnail records", count))
            .block();

        cleanupThumbnail(LocalThumbnail.class,
            thumbnail -> new UniqueKey(thumbnail.getSpec().getImageUri(),
                thumbnail.getSpec().getSize().name()))
            .flatMap(thumb -> {
                var filePath = localThumbnailService.toFilePath(thumb.getSpec().getFilePath());
                return deleteFile(filePath).thenReturn(thumb.getMetadata().getName());
            })
            .count()
            .doOnNext(count -> log.info("Deleted {} duplicate local thumbnail records.", count))
            .block();
        log.info("Duplicate thumbnails have been cleaned up.");
    }

    private Mono<Void> deleteFile(Path path) {
        return Mono.fromRunnable(
                () -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (Exception e) {
                        // Ignore
                    }
                })
            .subscribeOn(Schedulers.boundedElastic())
            .then();
    }

    private <T extends Extension> Flux<T> cleanupThumbnail(Class<T> thumbClass,
        Function<T, UniqueKey> keyFunction) {
        var unique = new HashSet<UniqueKey>();
        var duplicateThumbs = new ArrayList<T>();

        var collectDuplicateMono = extensionPaginatedOperator.list(thumbClass, new ListOptions())
            .doOnNext(thumbnail -> {
                var key = keyFunction.apply(thumbnail);
                if (unique.contains(key)) {
                    duplicateThumbs.add(thumbnail);
                } else {
                    unique.add(key);
                }
            })
            .then();

        return Mono.when(collectDuplicateMono)
            .thenMany(Flux.fromIterable(duplicateThumbs)
                .flatMap(this::deleteThumbnail)
            );
    }

    @SuppressWarnings("unchecked")
    private <T extends Extension> Mono<T> deleteThumbnail(T thumbnail) {
        return client.delete(thumbnail)
            .onErrorResume(OptimisticLockingFailureException.class,
                e -> deleteThumbnail((Class<T>) thumbnail.getClass(),
                    thumbnail.getMetadata().getName())
            );
    }

    private <T extends Extension> Mono<T> deleteThumbnail(Class<T> clazz, String name) {
        return Mono.defer(() -> client.fetch(clazz, name)
                .flatMap(client::delete)
            )
            .retryWhen(Retry.backoff(8, Duration.ofMillis(100))
                .filter(OptimisticLockingFailureException.class::isInstance));
    }

    record UniqueKey(String imageUri, String size) {
    }
}
