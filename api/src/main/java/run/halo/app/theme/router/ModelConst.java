package run.halo.app.theme.router;

/**
 * Static variable keys for view model.
 *
 * @author guqing
 * @since 2.0.0
 */
public enum ModelConst {
    ;
    public static final String TEMPLATE_ID = "_templateId";

    /**
     * Stable identity of the public resource represented by the current page.
     *
     * <p>The value is a {@code ResourceReference} for either an Extension or an owner-defined external resource. Route
     * owners should set it when a public page represents one localizable resource; collection, index and error pages
     * omit it.
     */
    public static final String RESOURCE_REFERENCE = "_resourceReference";

    public static final String POWERED_BY_HALO_TEMPLATE_ENGINE = "poweredByHaloTemplateEngine";

    /** This key is used to prevent caching from cache plugins. */
    public static final String NO_CACHE = "HALO_TEMPLATE_ENGINE.NO_CACHE";

    public static final Integer DEFAULT_PAGE_SIZE = 10;
}
