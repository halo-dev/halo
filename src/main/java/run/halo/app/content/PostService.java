package run.halo.app.content;

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

    Mono<ContentWrapper> getHeadContent(String postName);

    Mono<ContentWrapper> getReleaseContent(String postName);

    Mono<ContentWrapper> getContent(String snapshotName, String baseSnapshotName);
}
