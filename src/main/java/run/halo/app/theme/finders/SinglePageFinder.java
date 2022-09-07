package run.halo.app.theme.finders;

import org.springframework.lang.Nullable;
import run.halo.app.core.extension.SinglePage;
import run.halo.app.extension.ListResult;
import run.halo.app.theme.finders.vo.ContentVo;
import run.halo.app.theme.finders.vo.SinglePageVo;

/**
 * A finder for {@link SinglePage}.
 *
 * @author guqing
 * @since 2.0.0
 */
public interface SinglePageFinder {

    SinglePageVo getByName(String pageName);

    ContentVo content(String pageName);

    ListResult<SinglePageVo> list(@Nullable Integer page, @Nullable Integer size);
}
