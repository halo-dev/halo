package run.halo.app.theme.finders;

import java.util.List;
import run.halo.app.core.extension.Tag;
import run.halo.app.extension.ListResult;
import run.halo.app.theme.finders.vo.TagVo;

/**
 * A finder for {@link Tag}.
 *
 * @author guqing
 * @since 2.0.0
 */
public interface TagFinder {

    TagVo getByName(String name);

    ListResult<TagVo> list(int page, int size);

    List<TagVo> listAll();
}
