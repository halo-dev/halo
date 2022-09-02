package run.halo.app.extension.router;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;

/**
 * Use {@link IListRequest.QueryListRequest} instead.
 */
@Data
@Deprecated(forRemoval = true, since = "2.0.0")
public class ListRequest {

    @Schema(description = "The page number. Zero indicates no page.")
    private Integer page;

    @Schema(description = "Size of one page. Zero indicates no limit.")
    private Integer size;

    @Schema(description = "Label selector for filtering.")
    private List<String> labelSelector;

    @Schema(description = "Field selector for filtering.")
    private List<String> fieldSelector;

}
