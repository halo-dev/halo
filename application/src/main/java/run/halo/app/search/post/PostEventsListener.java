package run.halo.app.search.post;

import static run.halo.app.search.post.PostHaloDocumentsProvider.POST_DOCUMENT_TYPE;
import static run.halo.app.search.post.PostHaloDocumentsProvider.convert;

import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.content.PostService;
import run.halo.app.core.extension.content.Post;
import run.halo.app.event.post.PostDeletedEvent;
import run.halo.app.event.post.PostUpdatedEvent;
import run.halo.app.extension.ExtensionUtil;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.search.event.HaloDocumentAddRequestEvent;
import run.halo.app.search.event.HaloDocumentDeleteRequestEvent;

@Component
public class PostEventsListener {

    private final ApplicationEventPublisher publisher;

    private final PostService postService;

    private final ReactiveExtensionClient client;

    public PostEventsListener(
        ApplicationEventPublisher publisher,
        PostService postService,
        ReactiveExtensionClient client) {
        this.publisher = publisher;
        this.postService = postService;
        this.client = client;
    }

    @EventListener
    Mono<Void> onApplicationEvent(PostUpdatedEvent event) {
        return addOrUpdateOrDelete(event.getName());
    }

    @EventListener
    void onApplicationEvent(PostDeletedEvent event) {
        delete(event.getName());
    }

    private Mono<Void> addOrUpdateOrDelete(String postName) {
        return client.fetch(Post.class, postName)
            .flatMap(post -> {
                if (ExtensionUtil.isDeleted(post)) {
                    // if the post is deleted permanently, delete it.
                    return Mono.fromRunnable(() -> delete(postName));
                }
                // convert the post into halo document and add it to the search engine.
                return postService.getReleaseContent(post)
                    .map(content -> convert(post, content))
                    .doOnNext(haloDoc -> publisher.publishEvent(
                        new HaloDocumentAddRequestEvent(this, List.of(haloDoc))
                    ));
            })
            .then();
    }

    private void delete(String postName) {
        publisher.publishEvent(
            new HaloDocumentDeleteRequestEvent(this, List.of(POST_DOCUMENT_TYPE + '-' + postName))
        );
    }

}
