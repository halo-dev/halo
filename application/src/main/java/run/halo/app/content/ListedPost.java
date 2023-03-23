package run.halo.app.content;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;
import run.halo.app.core.extension.content.Category;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.content.Tag;

/**
 * An aggregate object of {@link Post} and {@link Category}
 * and {@link Tag} and more for post list.
 *
 * @author guqing
 * @since 2.0.0
 */
@Data
public class ListedPost {

    @Schema(required = true)
    private Post post;

    @Schema(required = true)
    private List<Category> categories;

    @Schema(required = true)
    private List<Tag> tags;

    @Schema(required = true)
    private List<Contributor> contributors;

    @Schema(required = true)
    private Contributor owner;

    @Schema(required = true)
    private Stats stats;
}
