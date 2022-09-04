package run.halo.app.theme.finders;

import java.util.List;
import org.springframework.lang.Nullable;
import run.halo.app.core.extension.Category;
import run.halo.app.extension.ListResult;
import run.halo.app.theme.finders.vo.CategoryTreeVo;
import run.halo.app.theme.finders.vo.CategoryVo;

/**
 * A finder for {@link Category}.
 *
 * @author guqing
 * @since 2.0.0
 */
public interface CategoryFinder {

    CategoryVo getByName(String name);

    List<CategoryVo> getByNames(List<String> names);

    ListResult<CategoryVo> list(@Nullable Integer page, @Nullable Integer size);

    List<CategoryVo> listAll();

    List<CategoryTreeVo> listAsTree();
}
