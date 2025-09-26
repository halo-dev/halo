package run.halo.app.theme.finders.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import run.halo.app.theme.finders.Finder;
import run.halo.app.theme.finders.ThumbnailFinder;

@Slf4j
@Finder("thumbnail")
@RequiredArgsConstructor
public class ThumbnailFinderImpl implements ThumbnailFinder {

    @Override
    public Mono<String> gen(String uriStr, String size) {
        // TODO Implement me
        return Mono.just(uriStr);
    }
}
