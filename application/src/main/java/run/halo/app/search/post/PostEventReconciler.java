package run.halo.app.search.post;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.Exceptions;
import run.halo.app.event.post.PostEvent;
import run.halo.app.event.post.PostPublishedEvent;
import run.halo.app.event.post.PostRecycledEvent;
import run.halo.app.event.post.PostUnpublishedEvent;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.DefaultController;
import run.halo.app.extension.controller.DefaultQueue;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.controller.RequestQueue;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;
import run.halo.app.search.PostDocUtils;
import run.halo.app.theme.finders.PostFinder;

@Slf4j
@Component
public class PostEventReconciler implements Reconciler<PostEvent>, SmartLifecycle {

    private final ExtensionGetter extensionGetter;

    private final PostFinder postFinder;

    private final RequestQueue<PostEvent> postEventQueue;


    private final Controller postEventController;

    private boolean running = false;

    public PostEventReconciler(ExtensionGetter extensionGetter,
        PostFinder postFinder) {
        this.extensionGetter = extensionGetter;
        this.postFinder = postFinder;

        postEventQueue = new DefaultQueue<>(Instant::now);
        postEventController = this.setupWith(null);
    }

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
            || postEvent instanceof PostRecycledEvent) {
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

    @EventListener(PostPublishedEvent.class)
    public void handlePostPublished(PostPublishedEvent publishedEvent) {
        postEventQueue.addImmediately(publishedEvent);
    }

    @EventListener(PostUnpublishedEvent.class)
    public void handlePostUnpublished(PostUnpublishedEvent unpublishedEvent) {
        postEventQueue.addImmediately(unpublishedEvent);
    }

    @EventListener(PostRecycledEvent.class)
    public void handlePostRecycled(PostRecycledEvent recycledEvent) {
        postEventQueue.addImmediately(recycledEvent);
    }

    void addPostDoc(String postName) throws InterruptedException {
        var latch = new CountDownLatch(1);
        var disposable = postFinder.getByName(postName)
            .map(PostDocUtils::from)
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
            .subscribe(service -> {
            }, throwable -> {
                throw Exceptions.propagate(throwable);
            });
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
            .subscribe(service -> {
            }, throwable -> {
                throw Exceptions.propagate(throwable);
            });
        try {
            latch.await();
        } finally {
            disposable.dispose();
        }
    }

    @Override
    public void start() {
        postEventController.start();
        running = true;
    }

    @Override
    public void stop() {
        running = false;
        postEventController.dispose();
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
