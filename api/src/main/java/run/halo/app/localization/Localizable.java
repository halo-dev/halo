package run.halo.app.localization;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares the display semantics of a serialized property. Names and current values never infer its behavior. A
 * top-level translation unit needs a stable {@link #key()}; properties inside a declared object or array use their
 * serialized property names. Undeclared object members remain source-owned.
 *
 * <pre>{@code
 * @Localizable(key = "heading", label = "Heading")
 * @LocalizableSearchTitle
 * private String title;
 *
 * @LocalizableSearchSummary
 * @Localizable(key = "summary", label = "Summary", multiline = true)
 * private String summary;
 *
 * @Localizable(key = "artwork", type = LocalizableSchema.Type.IMAGE)
 * private String image;
 *
 * @Localizable(key = "slides", type = LocalizableSchema.Type.ARRAY, identityField = "id")
 * private List<Slide> slides;
 * }</pre>
 *
 * @see LocalizableAnnotations
 * @since 2.28.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.RECORD_COMPONENT})
public @interface Localizable {
    /** Stable translation key; mandatory except for properties within a structured translation unit. */
    String key() default "";
    /**
     * Editor label as a message code for the owning Core or plugin message bundle. The declared value is used when no
     * message exists, so existing literal labels remain supported.
     */
    String label() default "";
    /** Explicit value semantics, independent of the Java property name. */
    LocalizableSchema.Type type() default LocalizableSchema.Type.TEXT;
    /** Permitted operation; DEFAULT uses the schema type's normal policy. */
    Operation policy() default Operation.DEFAULT;
    /** Representation for textual values; a raw document still needs a programmatic document binding. */
    LocalizableField.Format format() default LocalizableField.Format.PLAIN_TEXT;
    /** Requires a present, non-null final value, and nonblank text when applicable. */
    boolean required() default false;
    /** Selects a multiline editor for plain text. */
    boolean multiline() default false;
    /** Permits explicit JSON null; required values still reject null after projection. */
    boolean nullable() default true;
    /** Optional visual grouping; it has no behavioral meaning. */
    String group() default "";
    /** Explicit array item semantics; object items declare their properties with annotations. */
    LocalizableSchema.Type itemType() default LocalizableSchema.Type.OBJECT;
    /** Array item policy, using the item type's normal policy when DEFAULT. */
    Operation itemPolicy() default Operation.DEFAULT;
    /** Serialized name of a declared, preserved text or number property identifying array items. */
    String identityField() default "";
    /** Permits language-specific array structure under the normal schema identity checks. */
    boolean independentStructure() default false;

    /** Annotation policies, including an explicit type-dependent default. */
    enum Operation {
        /** Text permits translation, references/containers permit replacement, numbers/booleans are preserved. */
        DEFAULT,
        /** Text can be translated automatically. */
        TRANSLATE,
        /** Value can be replaced manually; containers retain child policies. */
        REPLACE,
        /** Value remains owned by the source and is never sent to an engine. */
        PRESERVE
    }
}
