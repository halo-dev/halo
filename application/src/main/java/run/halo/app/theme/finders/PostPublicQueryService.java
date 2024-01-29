package run.halo.app.theme.finders;

import org.springframework.lang.NonNull;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.PageRequest;
import run.halo.app.theme.ReactivePostContentHandler;
import run.halo.app.theme.finders.vo.ContentVo;
import run.halo.app.theme.finders.vo.ListedPostVo;
import run.halo.app.theme.finders.vo.PostVo;

public interface PostPublicQueryService {

    /**
     * Lists public posts by the given list options and page request.
     *
     * @param listOptions additional list options
     * @param page page request must not be null
     * @return a list of listed post vo
     */
    Mono<ListResult<ListedPostVo>> list(ListOptions listOptions, PageRequest page);

    /**
     * Converts post to listed post vo.
     *
     * @param post post must not be null
     * @return listed post vo
     */
    Mono<ListedPostVo> convertToListedVo(@NonNull Post post);

    /**
     * Converts {@link Post} to post vo and populate post content by the given snapshot name.
     * <p> This method will get post content by {@code snapshotName} and try to find
     * {@link ReactivePostContentHandler}s to extend the content</p>
     *
     * @param post post must not be null
     * @param snapshotName snapshot name must not be blank
     * @return converted post vo
     */
    Mono<PostVo> convertToVo(Post post, String snapshotName);

    /**
     * Gets post content by post name.
     * <p> This method will get post released content by post name and try to find
     * {@link ReactivePostContentHandler}s to extend the content</p>
     *
     * @param postName post name must not be blank
     * @return post content for theme-side
     * @see ReactivePostContentHandler
     */
    Mono<ContentVo> getContent(String postName);
}
