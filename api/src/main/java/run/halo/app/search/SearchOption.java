package run.halo.app.search;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import java.util.Map;
import lombok.Data;
import run.halo.app.localization.LocalizationContext;

/**
 * Search option. It is used to control search behavior.
 *
 * @author johnniang
 */
@Data
public class SearchOption {
    /**
     * BCP 47 content language used by a locale-aware search engine, for example {@code en} or {@code zh-Hant}. A null
     * value searches original documents only; it does not mean all languages.
     *
     * <p>{@link SearchService} derives this value from the subscription's server-selected {@link LocalizationContext},
     * replacing any locale supplied in the options. The public search endpoint therefore ignores this field in the
     * request body. An enabled localization context, a localized-document-capable engine and an enabled
     * {@link HaloDocumentVariantProvider} are required to search translated documents; otherwise the service searches
     * originals. Fallback languages are not queried separately by the search service.
     *
     * <p>A server-side caller that has already selected an allowed content language supplies the context on the
     * reactive subscription instead of setting this field:
     *
     * <pre>{@code
     * var context = new LocalizationContext(
     *     Locale.forLanguageTag("zh-Hans"), Locale.ENGLISH, List.of(), LocalizationScope.PUBLIC_API);
     * var option = new SearchOption();
     * option.setKeyword("welcome");
     * Mono<SearchResult> results = searchService.search(option)
     *     .contextWrite(reactorContext -> reactorContext.put(LocalizationContext.class, context));
     * }</pre>
     *
     * @since 2.28.0
     */
    @Schema(
            description = "BCP 47 content locale derived from the trusted localization context. "
                    + "The public search endpoint ignores locale in the request body. "
                    + "A null value searches original documents only.")
    @Pattern(regexp = "[a-zA-Z]{2,8}(-[a-zA-Z0-9]{1,8})*")
    private String locale;

    /** Search keyword. */
    @NotBlank
    private String keyword;

    /** Limit of result. */
    @Min(1)
    @Max(1000)
    private int limit = 10;

    /** Pre HTML tag of highlighted fragment. */
    private String highlightPreTag = "<B>";

    /** Post HTML tag of highlighted fragment. */
    private String highlightPostTag = "</B>";

    /** Whether to filter exposed content. If null, it will not filter. */
    private Boolean filterExposed;

    /** Whether to filter recycled content. If null, it will not filter. */
    private Boolean filterRecycled;

    /** Whether to filter published content. If null, it will not filter. */
    private Boolean filterPublished;

    /** Types to include(or). If null, it will include all types. */
    private List<String> includeTypes;

    /** Owner names to include(or). If null, it will include all owners. */
    private List<String> includeOwnerNames;

    /** Category names to include(and). If null, it will include all categories. */
    private List<String> includeCategoryNames;

    /** Tag names to include(and). If null, it will include all tags. */
    private List<String> includeTagNames;

    /** Additional annotations for extending search option by other search engines. */
    private Map<String, String> annotations;
}
