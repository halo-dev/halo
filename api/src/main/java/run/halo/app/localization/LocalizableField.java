package run.halo.app.localization;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Declares one display field that a resource owner permits to be translated.
 *
 * <p>The semantic key identifies the field in translation storage and remains separate from its location in the
 * resource. A pointer addresses a declared display value at an explicitly declared JSON location; it must not be used
 * to expose identifiers, credentials or operational settings. A null pointer denotes content resolved separately by
 * {@link ResourceLocalizationDescriptor#resolveFields}, such as a released snapshot's body.
 *
 * <p>Use a scalar schema for text and references, or an object/array schema for a structured translation unit. An
 * object or array carries one source fingerprint; its child policies still control which values may change. For
 * example, the fluent factories declare text and image semantics without inspecting either key:
 *
 * <pre>{@code
 * var fields = LocalizableField.list(
 *     LocalizableField.text("heading", "/spec/title").label("Title").required(true),
 *     LocalizableField.image("artwork", "/spec/cover").label("Cover"),
 *     LocalizableField.html("body", null).label("Body"),
 *     LocalizableField.raw("source", null));
 * var document = new LocalizableDocument("body", "source", "markdown", null);
 * }</pre>
 *
 * @param key nonblank semantic key, unique within the descriptor and stable across languages
 * @param pointer JSON pointer to a scalar, object or array display value in the source object, or null for aggregated
 *     content supplied by the resource descriptor
 * @param format representation of the field value; does not by itself select an editor or renderer
 * @param schema explicit value semantics and editing metadata
 * @param group optional group label message code or literal; no behavior is inferred from its contents
 * @since 2.28.0
 */
public record LocalizableField(String key, String pointer, Format format, LocalizableSchema schema, String group) {
    /**
     * Declares textual content without an alternate source location. HTML and plain text permit generation; raw source
     * is manually replaceable and participates in generation through its document binding.
     *
     * @param key stable semantic field key
     * @param pointer display leaf pointer, or null for separately resolved content
     * @param format field representation
     */
    public LocalizableField(String key, String pointer, Format format) {
        this(
                key,
                pointer,
                format,
                LocalizableSchema.text()
                        .format(format)
                        .policy(
                                format == Format.RAW_CONTENT
                                        ? LocalizableSchema.Policy.REPLACE
                                        : LocalizableSchema.Policy.TRANSLATE)
                        .build(),
                null);
    }

    /**
     * Validates the field declaration. Pointer validation rejects the document root; the resource owner remains
     * responsible for selecting the allowed display value and declaring its schema.
     *
     * @throws NullPointerException if the key, format or schema is null
     * @throws IllegalArgumentException if the key is blank, a pointer is not an absolute non-root JSON pointer, or the
     *     field and schema formats differ
     */
    public LocalizableField {
        Objects.requireNonNull(key);
        Objects.requireNonNull(format);
        Objects.requireNonNull(schema);
        if (format != schema.format()) {
            throw new IllegalArgumentException("Field format and schema format must agree");
        }
        if (key.isBlank()) {
            throw new IllegalArgumentException("Field key must not be blank");
        }
        if (!isDisplayPointer(pointer)) {
            throw new IllegalArgumentException(
                    "Fields must be semantic content or an explicitly declared non-root display pointer");
        }
        if (pointer != null) {
            com.fasterxml.jackson.core.JsonPointer.compile(pointer);
        }
    }

    /** Starts a plain-text field that permits generation and manual editing. */
    public static Builder text(String key, String pointer) {
        return new Builder(key, pointer, LocalizableSchema.text().build());
    }

    /** Starts an image-reference field that permits manual replacement but not text generation. */
    public static Builder image(String key, String pointer) {
        return new Builder(key, pointer, LocalizableSchema.image().build());
    }

    /** Starts a link-reference field whose value is validated as a URL, never translated as natural language. */
    public static Builder link(String key, String pointer) {
        return new Builder(key, pointer, LocalizableSchema.link().build());
    }

    /** Starts rendered HTML content; declare a document binding when it also has editable source. */
    public static Builder html(String key, String pointer) {
        return new Builder(
                key, pointer, LocalizableSchema.text().format(Format.HTML).build());
    }

    /** Starts editable source; its format and paired HTML field are supplied by a {@link LocalizableDocument}. */
    public static Builder raw(String key, String pointer) {
        return new Builder(
                key,
                pointer,
                LocalizableSchema.text()
                        .format(Format.RAW_CONTENT)
                        .policy(LocalizableSchema.Policy.REPLACE)
                        .build());
    }

    /** Starts an object translation unit; the supplied schema must have type {@link LocalizableSchema.Type#OBJECT}. */
    public static Builder object(String key, String pointer, LocalizableSchema schema) {
        if (schema.type() != LocalizableSchema.Type.OBJECT) {
            throw new IllegalArgumentException("Object fields require an object schema");
        }
        return new Builder(key, pointer, schema);
    }

    /** Starts an array with the supplied item schema; item count and order follow the source by default. */
    public static Builder array(String key, String pointer, LocalizableSchema items) {
        return new Builder(key, pointer, LocalizableSchema.array(items).build());
    }

    /** Starts an array and snapshots the item builder, avoiding a separate item build call. */
    public static Builder array(String key, String pointer, LocalizableSchema.Builder items) {
        return array(key, pointer, items.build());
    }

    /**
     * Builds a field list in declaration order, avoiding a separate build call for every field.
     *
     * <pre>{@code
     * return LocalizableField.list(
     *     text("heading", "/spec/heading").label("Heading").required(true),
     *     image("artwork", "/spec/artwork").label("Artwork"));
     * }</pre>
     *
     * @param fields field builders in display order
     * @return immutable field declarations, independently built from each builder
     */
    public static List<LocalizableField> list(Builder... fields) {
        return Arrays.stream(fields).map(Builder::build).toList();
    }

    /** Fluent, source-owned field declaration with no name-based behavior. */
    public static final class Builder {
        private final String key;
        private final String pointer;
        private LocalizableSchema schema;
        private String group;

        private Builder(String key, String pointer, LocalizableSchema schema) {
            this.key = key;
            this.pointer = pointer;
            this.schema = schema;
        }

        /** Replaces the complete value schema; the resulting field format follows that schema. */
        public Builder schema(LocalizableSchema value) {
            schema = value;
            return this;
        }

        /** Supplies a human-readable label without changing the field identity. */
        public Builder label(String value) {
            schema = schema.toBuilder().label(value).build();
            return this;
        }

        /** Requires a present, non-null value and nonblank text when applicable. */
        public Builder required(boolean value) {
            schema = schema.toBuilder().required(value).build();
            return this;
        }

        /** Selects multiline presentation for plain text; it does not change the value format. */
        public Builder multiline(boolean value) {
            schema = schema.toBuilder().multiline(value).build();
            return this;
        }

        /** Declares the permitted localization operation; container children retain their own policies. */
        public Builder policy(LocalizableSchema.Policy value) {
            schema = schema.toBuilder().policy(value).build();
            return this;
        }

        /** Controls whether a structured value or nested property may explicitly contain JSON null. */
        public Builder nullable(boolean value) {
            schema = schema.toBuilder().nullable(value).build();
            return this;
        }

        /** Selects an explicitly declared preserved property as an array item's identity. */
        public Builder identityField(String value) {
            schema = schema.toBuilder().identityField(value).build();
            return this;
        }

        /** Permits language-specific array structure, subject to source-owned member and identity checks. */
        public Builder independentStructure(boolean value) {
            schema = schema.toBuilder().independentStructure(value).build();
            return this;
        }

        /** Groups fields under a presentation label; the label has no special semantics. */
        public Builder group(String value) {
            group = value;
            return this;
        }

        /** Validates the declaration and creates an immutable field snapshot. */
        public LocalizableField build() {
            return new LocalizableField(key, pointer, schema.format(), schema, group);
        }
    }

    private static boolean isDisplayPointer(String pointer) {
        if (pointer == null) {
            return true;
        }
        return pointer.startsWith("/") && pointer.length() > 1;
    }

    /**
     * Representation of a translatable field, independent of its storage location.
     *
     * @since 2.28.0
     */
    public enum Format {
        /**
         * Text without markup, such as a title, label or description. A descriptor may mark a plain-text field as
         * multiline without changing its format.
         */
        PLAIN_TEXT,

        /**
         * Rendered HTML intended for display. When paired with editable source content, declare both fields in a
         * {@link LocalizableDocument} so they are projected together.
         */
        HTML,

        /**
         * Editable document source, such as Markdown or an editor's raw HTML. The document binding's
         * {@link LocalizableDocument#rawType()} identifies its format; this value does not imply Markdown or
         * automatically render the source to HTML.
         */
        RAW_CONTENT
    }
}
