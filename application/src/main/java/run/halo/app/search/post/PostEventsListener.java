package run.halo.app.search.post;

import static run.halo.app.search.post.PostHaloDocumentsProvider.POST_DOCUMENT_TYPE;
import static run.halo.app.search.post.PostHaloDocumentsProvider.convert;

import java.util.List;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import run.halo.app.content.PostService;
import run.halo.app.core.extension.content.Post;
import run.halo.app.event.post.PostDeletedEvent;
import run.halo.app.event.post.PostEvent;
import run.halo.app.event.post.PostPublishedEvent;
import run.halo.app.event.post.PostUnpublishedEvent;
import run.halo.app.event.post.PostUpdatedEvent;
import run.halo.app.extension.ExtensionUtil;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.search.HaloDocumentEventsListener;

@Component
public class PostEventsListener {

    private final HaloDocumentEventsListener indices;

    private final PostService postService;

    private final ReactiveExtensionClient client;

    public PostEventsListener(
            HaloDocumentEventsListener indices, PostService postService, ReactiveExtensionClient client) {
        this.indices = indices;
        this.postService = postService;
        this.client = client;
    }

    @EventListener({PostUpdatedEvent.class, PostPublishedEvent.class, PostUnpublishedEvent.class})
    void onApplicationEvent(PostEvent event) {
        refresh(event.getName());
    }

    @EventListener
    void onApplicationEvent(PostDeletedEvent event) {
        indices.deleteDocuments(List.of(documentId(event.getName())));
    }

    private void refresh(String postName) {
        indices.refreshDocument(
                documentId(postName),
                () -> client.fetch(Post.class, postName)
                        .filter(post -> !ExtensionUtil.isDeleted(post))
                        .flatMap(post -> postService.getReleaseContent(post).map(content -> convert(post, content))));
    }

    private static String documentId(String postName) {
        return POST_DOCUMENT_TYPE + '-' + postName;
    }
}
