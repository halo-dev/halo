package run.halo.app.theme.finders.impl;

import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import run.halo.app.core.attachment.ThumbnailSize;
import run.halo.app.core.attachment.thumbnail.ThumbnailService;
import run.halo.app.theme.finders.Finder;
import run.halo.app.theme.finders.ThumbnailFinder;

@Slf4j
@Finder("thumbnail")
@RequiredArgsConstructor
public class ThumbnailFinderImpl implements ThumbnailFinder {

    private final ThumbnailService thumbnailService;

    @Override
    public Mono<String> gen(String uriStr, String size) {
        return Mono.fromCallable(() -> URI.create(uriStr))
            .flatMap(uri -> thumbnailService.get(uri, ThumbnailSize.fromName(size)))
            .map(URI::toASCIIString)
            .onErrorComplete(IllegalArgumentException.class)
            .defaultIfEmpty(uriStr);
    }

}
