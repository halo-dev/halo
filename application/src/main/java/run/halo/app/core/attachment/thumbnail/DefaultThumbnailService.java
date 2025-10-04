package run.halo.app.core.attachment.thumbnail;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;
import run.halo.app.core.attachment.AttachmentChangedEvent;
import run.halo.app.core.attachment.ThumbnailSize;
import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.extension.ExtensionUtil;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.index.query.QueryFactory;

/**
 * Implementation of {@link ThumbnailService}.
 *
 * <p>
 * Caches thumbnail links in memory for better performance.
 *
 * @author johnniang
 * @since 2.22.0
 */
@Slf4j
@Component
class DefaultThumbnailService implements ThumbnailService {

    private static final Map<ThumbnailSize, URI> EMPTY_THUMBNAILS = Map.of();

    private final Cache<String, Map<ThumbnailSize, URI>> thumbnailCache;

    private final ReactiveExtensionClient client;

    public DefaultThumbnailService(ReactiveExtensionClient client) {
        this.client = client;
        this.thumbnailCache = Caffeine.newBuilder()
            // TODO make it configurable
            .maximumSize(10_000)
            .build();
    }

    @EventListener
    void handleAttachmentChangedEvent(AttachmentChangedEvent event) {
        updateCache(event.getAttachment());
    }

    void updateCache(Attachment attachment) {
        if (attachment.getStatus() == null) {
            return;
        }
        var permalink = attachment.getStatus().getPermalink();
        if (!StringUtils.hasText(permalink)) {
            return;
        }
        if (ExtensionUtil.isDeleted(attachment)) {
            thumbnailCache.invalidate(permalink);
            return;
        }
        var thumbnails = attachment.getStatus().getThumbnails();
        if (CollectionUtils.isEmpty(thumbnails)) {
            thumbnailCache.put(permalink, EMPTY_THUMBNAILS);
            return;
        }
        Map<ThumbnailSize, URI> validThumbnails = new HashMap<>();
        thumbnails.forEach((key, value) -> {
            var size = ThumbnailSize.optionalValueOf(key);
            if (size.isPresent() && StringUtils.hasText(value)) {
                validThumbnails.put(size.get(), URI.create(value));
            }
        });
        if (validThumbnails.isEmpty()) {
            thumbnailCache.put(permalink, EMPTY_THUMBNAILS);
        } else {
            thumbnailCache.put(permalink, Collections.unmodifiableMap(validThumbnails));
        }
    }

    @Override
    public Mono<URI> get(URI permalink, ThumbnailSize size) {
        return get(permalink).mapNotNull(thumbnails -> thumbnails.get(size));
    }

    @Override
    public Mono<Map<ThumbnailSize, URI>> get(URI permalink) {
        var permalinkString = permalink.toASCIIString();
        var thumbnails = thumbnailCache.getIfPresent(permalinkString);
        if (thumbnails != null) {
            return Mono.just(thumbnails);
        }
        // query from attachments
        var listOptions = ListOptions.builder()
            .andQuery(QueryFactory.equal("status.permalink", permalinkString))
            .build();
        return client.listAll(Attachment.class, listOptions, ExtensionUtil.defaultSort())
            .next()
            // Here we allow concurrent updates
            .doOnNext(this::updateCache)
            .mapNotNull(attachment -> this.thumbnailCache.getIfPresent(permalinkString))
            .switchIfEmpty(Mono.fromSupplier(() -> {
                // No attachment or no thumbnails, cache empty map to avoid cache miss again and
                // again.
                this.thumbnailCache.put(permalinkString, EMPTY_THUMBNAILS);
                return EMPTY_THUMBNAILS;
            }));
    }

}
