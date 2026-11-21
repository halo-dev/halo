package run.halo.app.localization;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Supplies catalog metadata for an explicitly registered, annotated resource class. Extension identities still use
 * their GVK and metadata name. Other resources require a namespace, type and {@link LocalizationId} property. This
 * annotation does not register an endpoint, authorize a read or enable classpath scanning.
 *
 * <pre>{@code
 * @LocalizableResource(namespace = "example.shop", type = "product", label = "Products")
 * record Product(
 *         @LocalizationId String id,
 *         @Localizable(key = "title")
 *         @LocalizableSearchTitle
 *         String title) {}
 * }</pre>
 *
 * @since 2.28.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface LocalizableResource {
    /** Resource-owner namespace, mandatory for non-Extension resources. */
    String namespace() default "";
    /** Stable type name within that namespace, mandatory for non-Extension resources. */
    String type() default "";
    /**
     * Catalog label as a message code for the owning Core or plugin message bundle. The declared value is used when no
     * message exists, so existing literal labels remain supported.
     */
    String label() default "";
}
