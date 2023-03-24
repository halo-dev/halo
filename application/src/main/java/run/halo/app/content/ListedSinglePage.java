package run.halo.app.content;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;
import run.halo.app.core.extension.content.SinglePage;


/**
 * An aggregate object of {@link SinglePage} and {@link Contributor} single page list.
 *
 * @author guqing
 * @since 2.0.0
 */
@Data
public class ListedSinglePage {

    @Schema(required = true)
    private SinglePage page;

    @Schema(required = true)
    private List<Contributor> contributors;

    @Schema(required = true)
    private Contributor owner;

    @Schema(required = true)
    private Stats stats;
}
