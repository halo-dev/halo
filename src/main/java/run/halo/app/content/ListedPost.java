package run.halo.app.content;

import java.util.List;
import lombok.Data;
import run.halo.app.core.extension.Category;
import run.halo.app.core.extension.Post;
import run.halo.app.core.extension.Tag;

/**
 * An aggregate object of {@link Post} and {@link Category}
 * and {@link Tag} and more for post list.
 *
 * @author guqing
 * @since 2.0.0
 */
@Data
public class ListedPost {

    private Post post;

    private List<Category> categories;

    private List<Tag> tags;

    private List<Contributor> contributors;
}
