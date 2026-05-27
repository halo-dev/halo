package run.halo.app.content;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;
import run.halo.app.core.extension.content.SinglePage;

/**
 * Aggregate object returned by the console single page list API.
 *
 * @author guqing
 * @since 2.0.0
 */
@Schema(description = "Single page list item with resolved contributors, owner, and counters.")
@Data
@Accessors(chain = true)
public class ListedSinglePage {

    @Schema(description = "Single page extension data.", requiredMode = REQUIRED)
    private SinglePage page;

    @Schema(description = "Users that have contributed to the single page content snapshots.", requiredMode = REQUIRED)
    private List<Contributor> contributors;

    @Schema(description = "Resolved owner of the single page.", requiredMode = REQUIRED)
    private Contributor owner;

    @Schema(
            description = "Aggregated visit, upvote, and comment counters for the single page.",
            requiredMode = REQUIRED)
    private Stats stats;
}
