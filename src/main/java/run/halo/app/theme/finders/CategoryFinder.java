package run.halo.app.theme.finders;

import run.halo.app.core.extension.Category;
import run.halo.app.extension.ListResult;
import run.halo.app.theme.finders.vo.CategoryVo;

/**
 * A finder for {@link Category}.
 *
 * @author guqing
 * @since 2.0.0
 */
public interface CategoryFinder {

    CategoryVo getByName(String name);

    ListResult<CategoryVo> list(int page, int size);
}
