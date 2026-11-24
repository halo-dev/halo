package run.halo.app.localization;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Source-owned value semantics shared by editors, translators and public projection.
 *
 * <p>Property names locate data only. Type and policy determine its behavior; names and values must never be used to
 * infer those semantics. Undeclared object properties remain source-owned. Array translations are fingerprinted as a
 * whole, even when an explicit identity permits matching reordered items.
 *
 * <p>Factories create optional, nullable values. Text permits generation; image and link references permit manual
 * replacement; numbers and booleans are preserved. Containers apply each declared child's policy. Required values must
 * remain present and non-null after projection; required strings must also be nonblank. An optional missing or null
 * container does not require its child properties.
 *
 * <pre>{@code
 * var slide = LocalizableSchema.object()
 *     .property("heading", LocalizableSchema.text().label("Heading"))
 *     .property("artwork", LocalizableSchema.image().label("Artwork"))
 *     .property("enabled", LocalizableSchema.bool().policy(Policy.PRESERVE))
 *     .build();
 * var slides = LocalizableSchema.array(slide).label("Slides").build();
 * }</pre>
 *
 * @param type actual value kind, independent of its JSON property name
 * @param policy permitted localization operation; containers apply child policies recursively
 * @param format textual representation; non-text types use {@code PLAIN_TEXT}
 * @param label optional human-readable field label
 * @param required whether the final value must be present and non-null, and nonblank when textual
 * @param multiline whether a plain-text editor should use multiple lines
 * @param nullable whether explicit JSON null has a valid source type; required values still reject null when projected
 * @param properties declared object properties in display order
 * @param items schema shared by array items, otherwise null
 * @param identityField explicitly declared item property used to match array items, otherwise null
 * @param independentStructure whether an array may add, remove or reorder items
 * @since 2.28.0
 */
public record LocalizableSchema(
        Type type,
        Policy policy,
        LocalizableField.Format format,
        String label,
        boolean required,
        boolean multiline,
        boolean nullable,
        Map<String, LocalizableSchema> properties,
        LocalizableSchema items,
        String identityField,
        boolean independentStructure) {
    /**
     * Creates an immutable declaration, preserving object property order. The declaration may contain at most twelve
     * nested container levels. An array identity must name a declared, preserved text or number property of its items.
     *
     * @throws IllegalArgumentException for incompatible type, format, policy, properties or item identity declarations
     * @throws NullPointerException when a mandatory declaration component is null
     */
    public LocalizableSchema {
        Objects.requireNonNull(type);
        Objects.requireNonNull(policy);
        Objects.requireNonNull(format);
        properties = Collections.unmodifiableMap(new LinkedHashMap<>(properties));
        properties.forEach((name, schema) -> {
            if (name == null || name.isBlank() || schema == null) {
                throw new IllegalArgumentException("Object properties require names and schemas");
            }
        });
        if (type != Type.OBJECT && !properties.isEmpty()) {
            throw new IllegalArgumentException("Only objects declare properties");
        }
        if ((type == Type.ARRAY) != (items != null)) {
            throw new IllegalArgumentException("Arrays require an item schema");
        }
        if (type != Type.ARRAY && (identityField != null || independentStructure)) {
            throw new IllegalArgumentException("Only arrays declare item identity or independent structure");
        }
        if (type != Type.TEXT && format != LocalizableField.Format.PLAIN_TEXT) {
            throw new IllegalArgumentException("Only text declares a content format");
        }
        if (policy == Policy.TRANSLATE && type != Type.TEXT) {
            throw new IllegalArgumentException("Only textual values can be machine translated directly");
        }
        validateIdentity(items, identityField);
        properties.values().forEach(child -> validateDepth(child, 1));
        if (items != null) {
            validateDepth(items, 1);
        }
    }

    private static void validateIdentity(LocalizableSchema items, String identityField) {
        if (identityField == null) {
            return;
        }
        Objects.requireNonNull(items);
        var identity = items.properties().get(identityField);
        if (items.type() != Type.OBJECT || identity == null || identity.policy() != Policy.PRESERVE) {
            throw new IllegalArgumentException("Array identity must reference a declared preserved property");
        }
        if (identity.type() != Type.TEXT && identity.type() != Type.NUMBER) {
            throw new IllegalArgumentException("Array identities must be text or numbers");
        }
    }

    private static void validateDepth(LocalizableSchema schema, int depth) {
        if (depth > 12) {
            throw new IllegalArgumentException("Localization schema is too deep");
        }
        schema.properties().values().forEach(child -> validateDepth(child, depth + 1));
        if (schema.items() != null) {
            validateDepth(schema.items(), depth + 1);
        }
    }

    /**
     * Returns whether this schema contains text explicitly permitted for ordinary generation. Raw editor content is
     * excluded because its generation requires a supported document format and corresponding renderer.
     */
    public boolean supportsGeneration() {
        if (policy == Policy.PRESERVE) {
            return false;
        }
        return switch (type) {
            case TEXT -> policy == Policy.TRANSLATE && format != LocalizableField.Format.RAW_CONTENT;
            case OBJECT -> properties.values().stream().anyMatch(LocalizableSchema::supportsGeneration);
            case ARRAY -> items.supportsGeneration();
            default -> false;
        };
    }

    /** Returns whether the wire value is JSON encoded rather than a plain string. */
    public boolean structured() {
        return type == Type.OBJECT || type == Type.ARRAY || type == Type.NUMBER || type == Type.BOOLEAN;
    }

    /** Starts a plain-text declaration that permits machine translation and manual editing. */
    public static Builder text() {
        return new Builder(Type.TEXT, Policy.TRANSLATE);
    }

    /** Starts an image-reference declaration that permits manual replacement. */
    public static Builder image() {
        return new Builder(Type.IMAGE, Policy.REPLACE);
    }

    /** Starts a public-link declaration that permits manual replacement. */
    public static Builder link() {
        return new Builder(Type.LINK, Policy.REPLACE);
    }

    /** Starts a JSON number declaration that preserves the source value by default. */
    public static Builder number() {
        return new Builder(Type.NUMBER, Policy.PRESERVE);
    }

    /** Starts a JSON boolean declaration that preserves the source value by default. */
    public static Builder bool() {
        return new Builder(Type.BOOLEAN, Policy.PRESERVE);
    }

    /** Starts an object declaration; undeclared properties remain owned by the source. */
    public static Builder object() {
        return new Builder(Type.OBJECT, Policy.REPLACE);
    }

    /** Starts an array declaration whose item count and order follow the source by default. */
    public static Builder array(LocalizableSchema items) {
        return new Builder(Type.ARRAY, Policy.REPLACE).items(items);
    }

    /** Starts an array using an immutable snapshot of the supplied item builder. */
    public static Builder array(Builder items) {
        return array(items.build());
    }

    /** Returns an independent builder initialized with this declaration's values. */
    public Builder toBuilder() {
        var builder = new Builder(type, policy);
        builder.format = format;
        builder.label = label;
        builder.required = required;
        builder.multiline = multiline;
        builder.nullable = nullable;
        builder.properties.putAll(properties);
        builder.items = items;
        builder.identityField = identityField;
        builder.independentStructure = independentStructure;
        return builder;
    }

    /** Typed fluent declaration; calling build creates an immutable snapshot. */
    public static final class Builder {
        private final Type type;
        private Policy policy;
        private LocalizableField.Format format = LocalizableField.Format.PLAIN_TEXT;
        private String label;
        private boolean required;
        private boolean multiline;
        private boolean nullable = true;
        private final Map<String, LocalizableSchema> properties = new LinkedHashMap<>();
        private LocalizableSchema items;
        private String identityField;
        private boolean independentStructure;

        private Builder(Type type, Policy policy) {
            this.type = type;
            this.policy = policy;
        }

        /** Sets the permitted operation; only text can directly use {@link Policy#TRANSLATE}. */
        public Builder policy(Policy value) {
            policy = value;
            return this;
        }

        /** Sets a text representation; non-text values must use {@link LocalizableField.Format#PLAIN_TEXT}. */
        public Builder format(LocalizableField.Format value) {
            format = value;
            return this;
        }

        /**
         * Sets the editor label message code without giving it any behavioral meaning. Core resolves it from the owning
         * message bundle and falls back to this literal value when no message exists.
         */
        public Builder label(String value) {
            label = value;
            return this;
        }

        /** Requires a non-null final value, and nonblank text, including when retained from the source. */
        public Builder required(boolean value) {
            required = value;
            return this;
        }

        /** Requests multiple input lines for a plain-text editor. */
        public Builder multiline(boolean value) {
            multiline = value;
            return this;
        }

        /** Allows explicit JSON null as a type; this does not override {@link #required(boolean)}. */
        public Builder nullable(boolean value) {
            nullable = value;
            return this;
        }

        /** Sets the shared schema for every array item. */
        public Builder items(LocalizableSchema value) {
            items = value;
            return this;
        }

        /**
         * Names a declared, preserved item property used to match source and localized array entries. Values must be
         * unique and non-null; textual identities must also be nonblank. This does not itself permit reordering.
         */
        public Builder identityField(String value) {
            identityField = value;
            return this;
        }

        /**
         * Permits an array to add, remove or reorder entries. Items containing preserved or undeclared source members
         * require an explicit identity so those members stay with the correct entry.
         */
        public Builder independentStructure(boolean value) {
            independentStructure = value;
            return this;
        }

        /** Adds an explicitly named object property in display order; duplicate names are rejected. */
        public Builder property(String name, LocalizableSchema value) {
            if (properties.putIfAbsent(name, value) != null) {
                throw new IllegalArgumentException("Duplicate object property");
            }
            return this;
        }

        /** Adds an object property using an immutable snapshot of its builder. */
        public Builder property(String name, Builder value) {
            return property(name, value.build());
        }

        /** Validates the declaration and creates an immutable snapshot. */
        public LocalizableSchema build() {
            return new LocalizableSchema(
                    type,
                    policy,
                    format,
                    label,
                    required,
                    multiline,
                    nullable,
                    properties,
                    items,
                    identityField,
                    independentStructure);
        }
    }

    /** Value semantics; these are not FormKit component names. */
    public enum Type {
        /** Human-readable text, including declared HTML or editable source. */
        TEXT,
        /** Image reference, edited as an image and never machine translated. */
        IMAGE,
        /** Public link, edited as a URL and never machine translated. */
        LINK,
        /** JSON number; preserved unless explicitly made replaceable. */
        NUMBER,
        /** JSON boolean; preserved unless explicitly made replaceable. */
        BOOLEAN,
        /** Named properties, with undeclared properties owned by the source. */
        OBJECT,
        /** Ordered items governed by one recursive schema. */
        ARRAY
    }

    /** Authority supplied by the resource owner, not inferred from field names or values. */
    public enum Policy {
        /** Text may be machine translated and manually edited. */
        TRANSLATE,
        /** Value may be manually localized; containers use each child's own policy. */
        REPLACE,
        /** Value remains source-owned and must not be changed or sent to a translation engine. */
        PRESERVE
    }
}
