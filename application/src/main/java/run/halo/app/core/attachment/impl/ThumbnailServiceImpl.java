package run.halo.app.core.attachment.impl;

import static run.halo.app.extension.index.query.QueryFactory.equal;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.core.attachment.LocalThumbnailProvider;
import run.halo.app.core.attachment.ThumbnailProvider;
import run.halo.app.core.attachment.ThumbnailProvider.ThumbnailContext;
import run.halo.app.core.attachment.ThumbnailService;
import run.halo.app.core.attachment.ThumbnailSize;
import run.halo.app.core.extension.attachment.Thumbnail;
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
    private final LocalThumbnailProvider localThumbnailProvider;
    private final ReactiveExtensionClient client;
    private final ExternalLinkProcessor externalLinkProcessor;

    @Override
    public Mono<URI> generate(URI imageUri, ThumbnailSize size) {
        var imageUrlOpt = toImageUrl(imageUri);
        if (imageUrlOpt.isEmpty()) {
            return Mono.empty();
        }
        var imageUrl = imageUrlOpt.get();
        return fetchThumbnail(imageUrl, size)
            .map(thumbnail -> URI.create(thumbnail.getSpec().getThumbnailUri()))
            .switchIfEmpty(create(imageUrl, size))
            .onErrorResume(Throwable.class, e -> {
                log.warn("Failed to generate thumbnail for image: {}", imageUrl, e);
                return Mono.just(URI.create(imageUrl.toString()));
            });
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

    Mono<URI> create(URL imageUrl, ThumbnailSize size) {
        var context = ThumbnailContext.builder()
            .imageUrl(imageUrl)
            .size(size)
            .build();
        return extensionGetter.getEnabledExtensions(ThumbnailProvider.class)
            .filterWhen(provider -> provider.supports(context))
            .next()
            .defaultIfEmpty(localThumbnailProvider)
            .flatMap(provider -> provider.generate(context))
            .flatMap(uri -> {
                var thumb = new Thumbnail();
                thumb.setMetadata(new Metadata());
                thumb.getMetadata().setGenerateName("thumb-");
                thumb.setSpec(new Thumbnail.Spec()
                    .setSize(size)
                    .setThumbnailUri(uri.toString())
                    .setImageUrl(imageUrl)
                    .setImageSignature(Thumbnail.signatureFor(imageUrl.toString()))
                );
                return client.create(thumb)
                    .thenReturn(uri);
            });
    }

    private Mono<Thumbnail> fetchThumbnail(URL imageUrl, ThumbnailSize size) {
        var imageHash = Thumbnail.signatureFor(imageUrl.toString());
        var id = Thumbnail.idIndexFunc(imageHash, size.name());
        return client.listBy(Thumbnail.class, ListOptions.builder()
                .fieldQuery(equal(Thumbnail.ID_INDEX, id))
                .build(), PageRequestImpl.ofSize(1))
            .flatMap(result -> Mono.justOrEmpty(ListResult.first(result)));
    }
}
