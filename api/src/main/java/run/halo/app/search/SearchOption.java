package run.halo.app.search;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import lombok.Data;

/**
 * Search option. It is used to control search behavior.
 *
 * @author johnniang
 */
@Data
public class SearchOption {
    /**
     * Search keyword.
     */
    @NotBlank
    private String keyword;

    /**
     * Limit of result.
     */
    @Min(1)
    @Max(1000)
    private int limit = 10;

    /**
     * Pre HTML tag of highlighted fragment.
     */
    private String highlightPreTag = "<B>";

    /**
     * Post HTML tag of highlighted fragment.
     */
    private String highlightPostTag = "</B>";

    /**
     * Whether to filter exposed content. If null, it will not filter.
     */
    private Boolean filterExposed;

    /**
     * Whether to filter recycled content. If null, it will not filter.
     */
    private Boolean filterRecycled;

    /**
     * Whether to filter published content. If null, it will not filter.
     */
    private Boolean filterPublished;

    /**
     * Types to include(or). If null, it will include all types.
     */
    private List<String> includeTypes;

    /**
     * Owner names to include(or). If null, it will include all owners.
     */
    private List<String> includeOwnerNames;

    /**
     * Category names to include(and). If null, it will include all categories.
     */
    private List<String> includeCategoryNames;

    /**
     * Tag names to include(and). If null, it will include all tags.
     */
    private List<String> includeTagNames;

    /**
     * Additional annotations for extending search option by other search engines.
     */
    private Map<String, String> annotations;
}
