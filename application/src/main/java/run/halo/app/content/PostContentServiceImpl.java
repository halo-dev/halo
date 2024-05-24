package run.halo.app.content;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Ref;

/**
 * Provides ability to get post content for the specified post.
 *
 * @author guqing
 * @since 2.16.0
 */
@Component
public class PostContentServiceImpl extends AbstractContentService implements PostContentService {
    private final ReactiveExtensionClient client;

    public PostContentServiceImpl(ReactiveExtensionClient client) {
        super(client);
        this.client = client;
    }

    @Override
    public Mono<ContentWrapper> getHeadContent(String postName) {
        return client.get(Post.class, postName)
            .flatMap(post -> {
                var headSnapshot = post.getSpec().getHeadSnapshot();
                return super.getContent(headSnapshot, post.getSpec().getBaseSnapshot());
            });
    }

    @Override
    public Mono<ContentWrapper> getReleaseContent(String postName) {
        return client.get(Post.class, postName)
            .flatMap(post -> {
                var releaseSnapshot = post.getSpec().getReleaseSnapshot();
                return super.getContent(releaseSnapshot, post.getSpec().getBaseSnapshot());
            });
    }

    @Override
    public Mono<ContentWrapper> getSpecifiedContent(String postName, String snapshotName) {
        return client.get(Post.class, postName)
            .flatMap(post -> {
                var baseSnapshot = post.getSpec().getBaseSnapshot();
                return super.getContent(snapshotName, baseSnapshot);
            });
    }

    @Override
    public Flux<String> listSnapshots(String postName) {
        return client.get(Post.class, postName)
            .flatMapMany(page -> listSnapshotsBy(Ref.of(page)))
            .map(snapshot -> snapshot.getMetadata().getName());
    }
}
