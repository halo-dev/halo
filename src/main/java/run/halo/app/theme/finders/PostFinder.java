package run.halo.app.theme.finders;

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

    ContentVo content(String postName);

    ListResult<PostVo> list(int page, int size);

    ListResult<PostVo> listByCategory(int page, int size, String categoryName);

    ListResult<PostVo> listByTag(int page, int size, String tag);
}
