package run.halo.app.theme.finders;

import org.springframework.lang.Nullable;
import run.halo.app.core.extension.Post;
import run.halo.app.extension.ListResult;
import run.halo.app.theme.finders.vo.ContentVo;
import run.halo.app.theme.finders.vo.PostVo;

/**
 * A finder for {@link Post}.
 *
 * @author guqing
 * @since 2.0.0
 */
public interface PostFinder {

    PostVo getByName(String postName);

    ContentVo content(String postName);

    ListResult<PostVo> list(@Nullable Integer page, @Nullable Integer size);

    ListResult<PostVo> listByCategory(@Nullable Integer page, @Nullable Integer size,
        String categoryName);

    ListResult<PostVo> listByTag(@Nullable Integer page, @Nullable Integer size, String tag);
}
