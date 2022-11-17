package run.halo.app.search.post;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import reactor.core.Exceptions;
import run.halo.app.event.post.PostPublishedEvent;
import run.halo.app.event.post.PostRecycledEvent;
import run.halo.app.event.post.PostUnpublishedEvent;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;
import run.halo.app.theme.finders.PostFinder;

@Component
public class PostEventListener {

    private final ExtensionGetter extensionGetter;

    private final PostFinder postFinder;

    public PostEventListener(ExtensionGetter extensionGetter,
        PostFinder postFinder) {
        this.extensionGetter = extensionGetter;
        this.postFinder = postFinder;
    }

    @Async
    @EventListener(PostPublishedEvent.class)
    public void handlePostPublished(PostPublishedEvent publishedEvent) throws InterruptedException {
        var latch = new CountDownLatch(1);
        postFinder.getByName(publishedEvent.getPostName())
            .map(PostDoc::from)
            .flatMap(postDoc -> extensionGetter.getEnabledExtension(PostSearchService.class)
                .doOnNext(searchService -> {
                    try {
                        searchService.addDocuments(List.of(postDoc));
                    } catch (Exception e) {
                        throw Exceptions.propagate(e);
                    }
                })
            )
            .doFinally(signalType -> latch.countDown())
            .subscribe();
        latch.await();
    }

    @Async
    @EventListener(PostUnpublishedEvent.class)
    public void handlePostUnpublished(PostUnpublishedEvent unpublishedEvent)
        throws InterruptedException {
        deletePostDoc(unpublishedEvent.getPostName());
    }

    @Async
    @EventListener(PostRecycledEvent.class)
    public void handlePostRecycled(PostRecycledEvent recycledEvent) throws InterruptedException {
        deletePostDoc(recycledEvent.getPostName());
    }

    void deletePostDoc(String postName) throws InterruptedException {
        var latch = new CountDownLatch(1);
        extensionGetter.getEnabledExtension(PostSearchService.class)
            .doOnNext(searchService -> {
                try {
                    searchService.removeDocuments(Set.of(postName));
                } catch (Exception e) {
                    throw Exceptions.propagate(e);
                }
            })
            .doFinally(signalType -> latch.countDown())
            .subscribe();
        latch.await();
    }
}
