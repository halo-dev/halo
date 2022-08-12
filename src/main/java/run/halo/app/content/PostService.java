package run.halo.app.content;

import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Post;

/**
 * @author guqing
 * @since 2.0.0
 */
public interface PostService {

    Mono<Post> draftPost(PostRequest postRequest);

    Mono<Post> updatePost(PostRequest postRequest);

    Mono<Post> publishPost(String postName);
}
