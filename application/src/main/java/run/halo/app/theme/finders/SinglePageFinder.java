package run.halo.app.theme.finders;

import java.util.Comparator;
import java.util.function.Predicate;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.extension.ListResult;
import run.halo.app.theme.finders.vo.ContentVo;
import run.halo.app.theme.finders.vo.ListedSinglePageVo;
import run.halo.app.theme.finders.vo.SinglePageVo;

/**
 * A finder for {@link SinglePage}.
 *
 * @author guqing
 * @since 2.0.0
 */
public interface SinglePageFinder {

    Mono<SinglePageVo> getByName(String pageName);

    Mono<ContentVo> content(String pageName);

    Mono<ListResult<ListedSinglePageVo>> list(@Nullable Integer page, @Nullable Integer size);

    Mono<ListResult<ListedSinglePageVo>> list(@Nullable Integer page, @Nullable Integer size,
        @Nullable Predicate<SinglePage> predicate, @Nullable Comparator<SinglePage> comparator);
}
