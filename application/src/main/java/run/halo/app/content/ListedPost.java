package run.halo.app.content;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;
import run.halo.app.core.extension.content.Category;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.content.Tag;

/**
 * Aggregate object returned by the console post list API.
 *
 * @author guqing
 * @since 2.0.0
 */
@Schema(description = "Post list item with resolved categories, tags, contributors, owner, and counters.")
@Data
@Accessors(chain = true)
public class ListedPost {

    @Schema(description = "Post extension data.", requiredMode = REQUIRED)
    private Post post;

    /** Resolved categories referenced by the post spec. */
    @Schema(requiredMode = REQUIRED)
    private List<Category> categories;

    /** Resolved tags referenced by the post spec. */
    @Schema(requiredMode = REQUIRED)
    private List<Tag> tags;

    @Schema(description = "Users that have contributed to the post content snapshots.", requiredMode = REQUIRED)
    private List<Contributor> contributors;

    @Schema(description = "Resolved owner of the post.", requiredMode = REQUIRED)
    private Contributor owner;

    @Schema(description = "Aggregated visit, upvote, and comment counters for the post.", requiredMode = REQUIRED)
    private Stats stats;
}
