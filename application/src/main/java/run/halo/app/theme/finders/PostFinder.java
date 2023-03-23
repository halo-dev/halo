package run.halo.app.theme.finders;

import org.springframework.lang.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.ListResult;
import run.halo.app.theme.finders.vo.ContentVo;
import run.halo.app.theme.finders.vo.ListedPostVo;
import run.halo.app.theme.finders.vo.NavigationPostVo;
import run.halo.app.theme.finders.vo.PostArchiveVo;
import run.halo.app.theme.finders.vo.PostVo;

/**
 * A finder for {@link Post}.
 *
 * @author guqing
 * @since 2.0.0
 */
public interface PostFinder {

    Mono<PostVo> getByName(String postName);

    Mono<ContentVo> content(String postName);

    Mono<NavigationPostVo> cursor(String current);

    Flux<ListedPostVo> listAll();

    Mono<ListResult<ListedPostVo>> list(@Nullable Integer page, @Nullable Integer size);

    Mono<ListResult<ListedPostVo>> listByCategory(@Nullable Integer page, @Nullable Integer size,
        String categoryName);

    Mono<ListResult<ListedPostVo>> listByTag(@Nullable Integer page, @Nullable Integer size,
        String tag);

    Mono<ListResult<ListedPostVo>> listByOwner(@Nullable Integer page, @Nullable Integer size,
        String owner);

    Mono<ListResult<PostArchiveVo>> archives(Integer page, Integer size);

    Mono<ListResult<PostArchiveVo>> archives(Integer page, Integer size, String year);

    Mono<ListResult<PostArchiveVo>> archives(Integer page, Integer size, String year, String month);
}
