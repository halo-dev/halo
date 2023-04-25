package run.halo.app.theme.finders;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.Predicate;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.ListResult;
import run.halo.app.theme.finders.vo.ListedPostVo;

public interface PostPublicQueryService {
    Predicate<Post> FIXED_PREDICATE = post -> post.isPublished()
        && Objects.equals(false, post.getSpec().getDeleted())
        && Post.VisibleEnum.PUBLIC.equals(post.getSpec().getVisible());


    /**
     * Lists posts page by predicate and comparator.
     *
     * @param page page number
     * @param size page size
     * @param postPredicate post predicate
     * @param comparator post comparator
     * @return list result
     */
    Mono<ListResult<ListedPostVo>> list(Integer page, Integer size,
        Predicate<Post> postPredicate,
        Comparator<Post> comparator);

    /**
     * Converts post to listed post vo.
     *
     * @param post post must not be null
     * @return listed post vo
     */
    Mono<ListedPostVo> convertToListedPostVo(@NonNull Post post);
}
