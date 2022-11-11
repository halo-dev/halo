package run.halo.app.search;

import org.springframework.stereotype.Service;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import run.halo.app.core.extension.Post;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;
import run.halo.app.search.post.PostDoc;
import run.halo.app.search.post.PostSearchService;
import run.halo.app.theme.finders.PostFinder;

@Service
public class IndicesServiceImpl implements IndicesService {
    private final ExtensionGetter extensionGetter;

    private final PostFinder postFinder;

    public IndicesServiceImpl(ExtensionGetter extensionGetter, PostFinder postFinder) {
        this.extensionGetter = extensionGetter;
        this.postFinder = postFinder;
    }

    @Override
    public Mono<Void> rebuildPostIndices() {
        return extensionGetter.getEnabledExtension(PostSearchService.class)
            // TODO Optimize listing posts with non-blocking.
            .flatMap(searchService -> Flux.fromStream(() -> postFinder.list(0, 0)
                    .stream()
                    .filter(post -> Post.isPublished(post.getMetadata()))
                    .peek(post -> postFinder.content(post.getMetadata().getName()))
                    .map(PostDoc::from))
                .subscribeOn(Schedulers.boundedElastic())
                .limitRate(100)
                .buffer(100)
                .doOnNext(postDocs -> {
                    try {
                        searchService.addDocuments(postDocs);
                    } catch (Exception e) {
                        throw Exceptions.propagate(e);
                    }
                })
                .then()
            );
    }
}
