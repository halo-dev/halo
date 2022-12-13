package run.halo.app.content.comment;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Ref;

/**
 * Comment subject for post.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class PostCommentSubject implements CommentSubject<Post> {

    private final ReactiveExtensionClient client;

    public PostCommentSubject(ReactiveExtensionClient client) {
        this.client = client;
    }

    @Override
    public Mono<Post> get(String name) {
        return client.fetch(Post.class, name);
    }

    @Override
    public boolean supports(Ref ref) {
        Assert.notNull(ref, "Subject ref must not be null.");
        GroupVersionKind groupVersionKind =
            new GroupVersionKind(ref.getGroup(), ref.getVersion(), ref.getKind());
        return GroupVersionKind.fromExtension(Post.class).equals(groupVersionKind);
    }
}
