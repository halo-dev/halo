package run.halo.app.theme.finders.impl;

import java.net.URI;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import run.halo.app.core.attachment.ThumbnailService;
import run.halo.app.core.attachment.ThumbnailSize;
import run.halo.app.theme.finders.Finder;
import run.halo.app.theme.finders.ThumbnailFinder;

@Finder("thumbnail")
@RequiredArgsConstructor
public class ThumbnailFinderImpl implements ThumbnailFinder {
    private final ThumbnailService thumbnailService;

    @Override
    public Mono<String> gen(String uriStr, String size) {
        return thumbnailService.generate(URI.create(uriStr), ThumbnailSize.fromName(size))
            .map(URI::toString)
            .defaultIfEmpty(uriStr);
    }
}
