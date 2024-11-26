package run.halo.app.core.attachment.impl;

import static run.halo.app.extension.index.query.QueryFactory.equal;
import static run.halo.app.extension.index.query.QueryFactory.startsWith;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;
import run.halo.app.core.attachment.AttachmentUtils;
import run.halo.app.core.attachment.LocalThumbnailService;
import run.halo.app.core.attachment.ThumbnailProvider;
import run.halo.app.core.attachment.ThumbnailProvider.ThumbnailContext;
import run.halo.app.core.attachment.ThumbnailService;
import run.halo.app.core.attachment.ThumbnailSigner;
import run.halo.app.core.attachment.ThumbnailSize;
import run.halo.app.core.attachment.extension.Thumbnail;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.ExternalLinkProcessor;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;

@Slf4j
@Component
@RequiredArgsConstructor
public class ThumbnailServiceImpl implements ThumbnailService {
    private final ExtensionGetter extensionGetter;
    private final ReactiveExtensionClient client;
    private final ExternalLinkProcessor externalLinkProcessor;
    private final ThumbnailProvider thumbnailProvider;
    private final LocalThumbnailService localThumbnailService;
    private final Map<CacheKey, Mono<URI>> ongoingTasks = new ConcurrentHashMap<>();

    @Override
    public Mono<URI> generate(URI imageUri, ThumbnailSize size) {
        var cacheKey = new CacheKey(imageUri, size);
        // Combine caching to implement more elegant deduplication logic, ensure that only
        // one thread executes the logic of create at the same time, and there is no global lock
        // restriction
        return ongoingTasks.computeIfAbsent(cacheKey, k -> doGenerate(imageUri, size)
            // In the case of concurrency, doGenerate must return the same instance
            .cache()
            .doFinally(signalType -> ongoingTasks.remove(cacheKey)));
    }

    record CacheKey(URI imageUri, ThumbnailSize size) {
    }

    private Mono<URI> doGenerate(URI imageUri, ThumbnailSize size) {
        var imageUrlOpt = toImageUrl(imageUri);
        if (imageUrlOpt.isEmpty()) {
            return Mono.empty();
        }
        var imageUrl = imageUrlOpt.get();
        return fetchThumbnail(imageUri, size)
            .map(thumbnail -> URI.create(thumbnail.getSpec().getThumbnailUri()))
            .switchIfEmpty(Mono.defer(() -> create(imageUrl, size)))
            .onErrorResume(Throwable.class, e -> {
                log.warn("Failed to generate thumbnail for image: {}", imageUrl, e);
                return Mono.just(URI.create(imageUrl.toString()));
            });
    }

    @Override
    public Mono<URI> get(URI imageUri, ThumbnailSize size) {
        return fetchThumbnail(imageUri, size)
            .map(thumbnail -> URI.create(thumbnail.getSpec().getThumbnailUri()))
            .defaultIfEmpty(imageUri);
    }

    @Override
    public Mono<Void> delete(URI imageUri) {
        Assert.notNull(imageUri, "Image uri must not be null");
        Mono<Void> deleteMono;
        if (imageUri.isAbsolute()) {
            deleteMono = thumbnailProvider.delete(AttachmentUtils.toUrl(imageUri));
        } else {
            // Local thumbnails maybe a relative path, so we need to process it.
            deleteMono = localThumbnailService.delete(imageUri);
        }
        return deleteMono.then(deleteThumbnailRecord(imageUri));
    }

    private Mono<Void> deleteThumbnailRecord(URI imageUri) {
        var imageHash = signatureFor(imageUri);
        var listOptions = ListOptions.builder()
            .fieldQuery(startsWith(Thumbnail.ID_INDEX, Thumbnail.idIndexFunc(imageHash, "")))
            .build();
        return client.listAll(Thumbnail.class, listOptions, Sort.unsorted())
            .flatMap(client::delete)
            .then();
    }

    Optional<URL> toImageUrl(URI imageUri) {
        try {
            if (imageUri.isAbsolute()) {
                return Optional.of(imageUri.toURL());
            }
            var url = new URL(externalLinkProcessor.processLink(imageUri.toString()));
            return Optional.of(url);
        } catch (MalformedURLException e) {
            // Ignore
        }
        return Optional.empty();
    }

    protected Mono<URI> create(URL imageUrl, ThumbnailSize size) {
        var context = ThumbnailContext.builder()
            .imageUrl(imageUrl)
            .size(size)
            .build();
        var imageUri =
            localThumbnailService.ensureInSiteUriIsRelative(URI.create(imageUrl.toString()));
        return extensionGetter.getEnabledExtensions(ThumbnailProvider.class)
            .filterWhen(provider -> provider.supports(context))
            .next()
            .flatMap(provider -> provider.generate(context))
            .flatMap(uri -> {
                var thumb = new Thumbnail();
                thumb.setMetadata(new Metadata());
                thumb.getMetadata().setGenerateName("thumb-");
                thumb.setSpec(new Thumbnail.Spec()
                    .setSize(size)
                    .setThumbnailUri(uri.toASCIIString())
                    .setImageUri(imageUri.toASCIIString())
                    .setImageSignature(signatureFor(imageUri))
                );
                // double check
                return fetchThumbnail(imageUri, size)
                    .map(thumbnail -> URI.create(thumbnail.getSpec().getThumbnailUri()))
                    .switchIfEmpty(Mono.defer(() -> client.create(thumb)
                        .thenReturn(uri))
                    );
            });
    }

    private String signatureFor(URI imageUri) {
        var uri = localThumbnailService.ensureInSiteUriIsRelative(imageUri);
        return ThumbnailSigner.generateSignature(uri);
    }

    Mono<Thumbnail> fetchThumbnail(URI imageUri, ThumbnailSize size) {
        var imageHash = signatureFor(imageUri);
        var id = Thumbnail.idIndexFunc(imageHash, size.name());
        return client.listBy(Thumbnail.class, ListOptions.builder()
                .fieldQuery(equal(Thumbnail.ID_INDEX, id))
                .build(), PageRequestImpl.ofSize(1))
            .flatMap(result -> Mono.justOrEmpty(ListResult.first(result)));
    }
}
