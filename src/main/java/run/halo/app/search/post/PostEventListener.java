package run.halo.app.search.post;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import reactor.core.Exceptions;
import run.halo.app.event.post.PostDeletedEvent;
import run.halo.app.event.post.PostEvent;
import run.halo.app.event.post.PostPublishedEvent;
import run.halo.app.event.post.PostRecycledEvent;
import run.halo.app.event.post.PostUnpublishedEvent;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.DefaultController;
import run.halo.app.extension.controller.DefaultDelayQueue;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.controller.RequestQueue;
import run.halo.app.infra.SchemeInitializedEvent;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;
import run.halo.app.theme.finders.PostFinder;

@Component
public class PostEventListener implements DisposableBean {

    private final ExtensionGetter extensionGetter;

    private final PostFinder postFinder;

    private final RequestQueue<PostEvent> postEventQueue;


    private final Controller postEventController;

    public PostEventListener(ExtensionGetter extensionGetter,
        PostFinder postFinder) {
        this.extensionGetter = extensionGetter;
        this.postFinder = postFinder;

        postEventQueue = new DefaultDelayQueue<>(Instant::now);
        postEventController = new PostEventReconciler().setupWith(null);
    }

    class PostEventReconciler implements Reconciler<PostEvent> {

        @Override
        public Result reconcile(PostEvent postEvent) {
            if (postEvent instanceof PostPublishedEvent) {
                try {
                    addPostDoc(postEvent.getName());
                } catch (InterruptedException e) {
                    throw Exceptions.propagate(e);
                }
            }
            if (postEvent instanceof PostUnpublishedEvent
                || postEvent instanceof PostDeletedEvent) {
                try {
                    deletePostDoc(postEvent.getName());
                } catch (InterruptedException e) {
                    throw Exceptions.propagate(e);
                }
            }
            return null;
        }

        @Override
        public Controller setupWith(ControllerBuilder builder) {
            return new DefaultController<>(
                this.getClass().getName(),
                this,
                postEventQueue,
                null,
                Duration.ofMillis(100),
                Duration.ofSeconds(1000)
            );
        }
    }

    @Override
    public void destroy() throws Exception {
        postEventController.dispose();
    }

    @EventListener(SchemeInitializedEvent.class)
    public void handleSchemeInitializedEvent(SchemeInitializedEvent event) {
        postEventController.start();
    }

    @Async
    @EventListener(PostPublishedEvent.class)
    public void handlePostPublished(PostPublishedEvent publishedEvent) {
        postEventQueue.addImmediately(publishedEvent);
    }

    @Async
    @EventListener(PostUnpublishedEvent.class)
    public void handlePostUnpublished(PostUnpublishedEvent unpublishedEvent) {
        postEventQueue.addImmediately(unpublishedEvent);
    }

    @Async
    @EventListener(PostRecycledEvent.class)
    public void handlePostRecycled(PostRecycledEvent recycledEvent) {
        postEventQueue.addImmediately(recycledEvent);
    }

    void addPostDoc(String postName) throws InterruptedException {
        var latch = new CountDownLatch(1);
        var disposable = postFinder.getByName(postName)
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
        try {
            latch.await();
        } finally {
            disposable.dispose();
        }
    }

    void deletePostDoc(String postName) throws InterruptedException {
        var latch = new CountDownLatch(1);
        var disposable = extensionGetter.getEnabledExtension(PostSearchService.class)
            .doOnNext(searchService -> {
                try {
                    searchService.removeDocuments(Set.of(postName));
                } catch (Exception e) {
                    throw Exceptions.propagate(e);
                }
            })
            .doFinally(signalType -> latch.countDown())
            .subscribe();
        try {
            latch.await();
        } finally {
            disposable.dispose();
        }
    }
}
