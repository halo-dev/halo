package run.halo.app.search;

import org.springframework.stereotype.Service;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import run.halo.app.core.extension.content.Post;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;
import run.halo.app.search.post.PostSearchService;
import run.halo.app.theme.finders.PostFinder;
import run.halo.app.theme.finders.vo.PostVo;

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
            .flatMap(searchService -> Mono.fromRunnable(
                    () -> {
                        try {
                            // remove all docs before rebuilding
                            searchService.removeAllDocuments();
                        } catch (Exception e) {
                            throw Exceptions.propagate(e);
                        }
                    })
                .then(rebuildPostIndices(searchService))
            )
            .subscribeOn(Schedulers.boundedElastic());
    }

    private Mono<Void> rebuildPostIndices(PostSearchService searchService) {
        return postFinder.listAll()
            .filter(
                post -> Post.isPublished(post.getMetadata()) && Post.isPublic(post.getSpec()))
            .flatMap(listedPostVo -> {
                PostVo postVo = PostVo.from(listedPostVo);
                return postFinder.content(postVo.getMetadata().getName())
                    .map(content -> {
                        postVo.setContent(content);
                        return postVo;
                    })
                    .defaultIfEmpty(postVo);
            })
            .map(PostDocUtils::from)
            .limitRate(100)
            .buffer(100)
            .doOnNext(postDocs -> {
                try {
                    searchService.addDocuments(postDocs);
                } catch (Exception e) {
                    throw Exceptions.propagate(e);
                }
            })
            .then();
    }
}
