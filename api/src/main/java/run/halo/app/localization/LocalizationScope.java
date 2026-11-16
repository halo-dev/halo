package run.halo.app.localization;

/**
 * Identifies the consumer of a localized display projection.
 *
 * <p>The scope is carried by {@link LocalizationContext}; it does not select a language, grant read permissions or
 * change stored resources. Callers must enforce access permissions before requesting a projection.
 * {@link LocalizationContext#enabled()} disables localization for {@link #CONSOLE} and {@link #INTERNAL}, even when the
 * target language differs from the source language.
 *
 * @since 2.28.0
 */
public enum LocalizationScope {
    /**
     * A visitor-facing page rendered by a theme, including data obtained through its finders. Allows published resource
     * translations and localized display settings for that page.
     */
    PUBLIC_PAGE,

    /**
     * A public content API response requested in a particular language. Allows published resource translations and
     * localized display settings; this is not the scope for Console APIs or arbitrary requests whose path starts with
     * {@code /apis}.
     */
    PUBLIC_API,

    /**
     * A language-specific search document built from published content. Allows resource and semantic-field projections
     * without an HTTP request. Display settings are not localized in this scope. The indexing caller supplies the
     * language explicitly.
     */
    SEARCH_INDEX,

    /**
     * An administrative read that needs original resource values for viewing or editing. Explicitly disables
     * projection; ordinary Console reads need not create a localization context.
     */
    CONSOLE,

    /**
     * An internal operation that needs original values, such as source hashing or reconciliation. Explicitly disables
     * projection. An indexing operation that intentionally needs a translated variant should use {@link #SEARCH_INDEX}
     * instead.
     */
    INTERNAL
}
