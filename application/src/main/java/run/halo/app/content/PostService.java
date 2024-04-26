package run.halo.app.content;

import org.springframework.lang.NonNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.ListResult;

/**
 * Service for {@link Post}.
 *
 * @author guqing
 * @since 2.0.0
 */
public interface PostService {

    Mono<ListResult<ListedPost>> listPost(PostQuery query);

    Mono<Post> draftPost(PostRequest postRequest);

    Mono<Post> updatePost(PostRequest postRequest);

    Mono<Post> updateBy(@NonNull Post post);

    Mono<ContentWrapper> getHeadContent(String postName);

    Mono<ContentWrapper> getHeadContent(Post post);

    Mono<ContentWrapper> getReleaseContent(String postName);

    Mono<ContentWrapper> getReleaseContent(Post post);

    Mono<ContentWrapper> getContent(String snapshotName, String baseSnapshotName);

    Flux<ListedSnapshotDto> listSnapshots(String name);

    Mono<Post> publish(Post post);

    Mono<Post> unpublish(Post post);

    /**
     * Get post by username.
     *
     * @param postName is post name.
     * @param username is username.
     * @return full post data or empty.
     */
    Mono<Post> getByUsername(String postName, String username);

    Mono<Post> revertToSpecifiedSnapshot(String postName, String snapshotName);

    Mono<ContentWrapper> deleteContent(String postName, String snapshotName);
}
