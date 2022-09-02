package run.halo.app.theme.finders;

import java.util.List;
import org.springframework.lang.Nullable;
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

    List<TagVo> getByNames(List<String> names);

    ListResult<TagVo> list(@Nullable Integer page, @Nullable Integer size);

    List<TagVo> listAll();
}
