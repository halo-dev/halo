package run.halo.app.localization;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Declares string-valued display fields in an array whose entries have stable, source-owned identities.
 *
 * <p>For example, a resource owner may expose the content of named metadata entries:
 *
 * <pre>{@code
 * LocalizableFieldCollection.builder("htmlMetas", "/spec/htmlMetas")
 *     .valueField("content")
 *     .group("Search and sharing")
 *     .entries("name", entries -> entries
 *         .add("description", LocalizableSchema.text().label("Search description").multiline(true))
 *         .add("keywords", LocalizableSchema.text().label("Search keywords")))
 *     .entries("property", entries -> entries
 *         .add("og:title", LocalizableSchema.text().label("Sharing title")))
 *     .build();
 * }</pre>
 *
 * <p>The identity fields are alternatives: an entry must contain exactly one nonblank textual identity. Missing,
 * ambiguous or duplicate identities are ignored. Translation keys retain the identity's case and never contain an array
 * index. Resolve against the current source each time, so reordering cannot move a translation to another entry. Only
 * existing textual value leaves are exposed; identities and array structure remain unchanged.
 *
 * <p>Entry definitions explicitly select type and policy: text can permit generation, image and link references permit
 * manual replacement, and preserved values remain source-owned. Unrecognized entries use replaceable text and do not
 * permit automatic generation. Providers must respect these declarations rather than infer semantics from names or
 * value contents. A source owner can explicitly preserve operational values or declare further translatable entries.
 *
 * @param namespace stable collection name, unique within the resource descriptor
 * @param pointer JSON pointer to a declared display array
 * @param identityFields alternative object property names identifying an entry
 * @param valueField object property containing the textual display value
 * @param fallbackFormat representation used only for unrecognized string-valued entries; explicitly defined entries use
 *     their own schema's format
 * @param definitions exact identity declarations supplying value semantics; unknown entries are manually replaceable
 *     text and are not automatically machine translated
 * @param group optional human-readable group label
 * @since 2.28.0
 */
public record LocalizableFieldCollection(
        String namespace,
        String pointer,
        List<String> identityFields,
        String valueField,
        LocalizableField.Format fallbackFormat,
        List<EntryDefinition> definitions,
        String group) {

    /**
     * Declares a collection without per-entry semantics or a display group. Its resolved fields permit manual text
     * replacement, but not automatic generation.
     */
    public LocalizableFieldCollection(
            String namespace,
            String pointer,
            List<String> identityFields,
            String valueField,
            LocalizableField.Format fallbackFormat) {
        this(namespace, pointer, identityFields, valueField, fallbackFormat, List.of(), null);
    }

    /**
     * Creates an immutable collection declaration. Identity attributes must be unique and distinct from the value
     * attribute; each exact entry identity can have only one definition.
     *
     * @throws IllegalArgumentException for blank names, invalid pointers or conflicting identity definitions
     * @throws NullPointerException when a mandatory declaration component is null
     */
    public LocalizableFieldCollection {
        requireName(namespace);
        requireName(valueField);
        Objects.requireNonNull(fallbackFormat);
        Objects.requireNonNull(pointer);
        if (!pointer.startsWith("/") || pointer.length() < 2) {
            throw new IllegalArgumentException("A collection requires an absolute non-root JSON pointer");
        }
        JsonPointer.compile(pointer);
        identityFields = List.copyOf(identityFields);
        definitions = List.copyOf(definitions);
        identityFields.forEach(LocalizableFieldCollection::requireName);
        if (identityFields.isEmpty() || new HashSet<>(identityFields).size() != identityFields.size()) {
            throw new IllegalArgumentException("Identity fields must be nonempty and unique");
        }
        if (identityFields.contains(valueField)) {
            throw new IllegalArgumentException("A value must not be an identity field");
        }
        var defined = new HashSet<List<String>>();
        for (var definition : definitions) {
            if (!identityFields.contains(definition.identityField())) {
                throw new IllegalArgumentException("Definition uses an undeclared identity attribute");
            }
            if (!defined.add(List.of(definition.identityField(), definition.identityValue()))) {
                throw new IllegalArgumentException("Duplicate collection entry definition");
            }
        }
    }

    /**
     * Starts a collection declaration. The value property must be specified; identity properties are registered by
     * {@link Builder#entries} or {@link Builder#identityFields}. Unrecognized entries default to plain-text
     * replacement.
     *
     * @param namespace stable collection name used in translation keys
     * @param pointer location of the source display array
     * @return an independent mutable builder
     */
    public static Builder builder(String namespace, String pointer) {
        return new Builder(namespace, pointer);
    }

    /** Declares collection structure once, then groups value schemas by their source identity attribute. */
    public static final class Builder {
        private final String namespace;
        private final String pointer;
        private final LinkedHashSet<String> identityFields = new LinkedHashSet<>();
        private final List<EntryDefinition> definitions = new ArrayList<>();
        private String valueField;
        private LocalizableField.Format fallbackFormat = LocalizableField.Format.PLAIN_TEXT;
        private String group;

        private Builder(String namespace, String pointer) {
            this.namespace = namespace;
            this.pointer = pointer;
        }

        /** Sets the object property holding each entry's string value. */
        public Builder valueField(String value) {
            valueField = value;
            return this;
        }

        /** Sets the optional group message code or literal shared by the collection's resolved fields. */
        public Builder group(String value) {
            group = value;
            return this;
        }

        /**
         * Registers alternative identity properties without defining any entries. This is useful for exposing
         * candidates that inherit source values until explicitly selected; entries() already registers its property.
         */
        public Builder identityFields(String... fields) {
            for (var field : fields) {
                requireName(field);
                identityFields.add(field);
            }
            return this;
        }

        /**
         * Registers one identity property and snapshots its entry definitions. Repeated calls can append entries for
         * the same property, but duplicate exact identities are rejected when building the collection. Resolved fields
         * follow source array order, independently of the order of these groups.
         *
         * @param identityField source identity property, such as name or property
         * @param configure declares the exact identity values and their schemas
         * @return this builder for further declarations
         */
        public Builder entries(String identityField, Consumer<Entries> configure) {
            requireName(identityField);
            var entries = new Entries(identityField);
            configure.accept(entries);
            identityFields.add(identityField);
            definitions.addAll(entries.definitions);
            return this;
        }

        /**
         * Changes only the format of unrecognized entries. They remain manually replaceable and never gain automatic
         * generation permission through this fallback. Defaults to {@code PLAIN_TEXT}.
         */
        public Builder fallbackFormat(LocalizableField.Format value) {
            fallbackFormat = Objects.requireNonNull(value);
            return this;
        }

        /** Returns an immutable validated snapshot; subsequent builder changes do not modify it. */
        public LocalizableFieldCollection build() {
            return new LocalizableFieldCollection(
                    namespace, pointer, List.copyOf(identityFields), valueField, fallbackFormat, definitions, group);
        }
    }

    /** Entry declarations for one identity property; callers specify each identity value and schema only. */
    public static final class Entries {
        private final String identityField;
        private final List<EntryDefinition> definitions = new ArrayList<>();

        private Entries(String identityField) {
            this.identityField = identityField;
        }

        /** Adds the exact identity value with an immutable string-valued schema. */
        public Entries add(String identityValue, LocalizableSchema schema) {
            definitions.add(new EntryDefinition(identityField, identityValue, schema));
            return this;
        }

        /** Snapshots a schema builder, avoiding a separate build call for each entry. */
        public Entries add(String identityValue, LocalizableSchema.Builder schema) {
            return add(identityValue, schema.build());
        }
    }

    /**
     * Resolves the current locations of unambiguous textual entries without changing the source. Duplicate identities
     * are excluded even when one occurrence has no textual value.
     *
     * @param source source resource JSON
     * @return immutable list of stable keys and current pointers; empty for a missing or non-array collection
     */
    public List<LocalizableField> resolve(JsonNode source) {
        var array = source.at(pointer);
        if (!array.isArray()) {
            return List.of();
        }
        var fields = new LinkedHashMap<String, LocalizableField>();
        var seen = new HashSet<String>();
        var duplicates = new HashSet<String>();
        for (int index = 0; index < array.size(); index++) {
            var entry = array.get(index);
            var key = identityKey(entry);
            if (key == null) {
                continue;
            }
            if (!seen.add(key)) {
                duplicates.add(key);
            }
            if (entry.path(valueField).isTextual()) {
                var identity = identityFields.stream()
                        .filter(name -> !entry.path(name).asText("").isBlank())
                        .findFirst()
                        .orElseThrow();
                var schema = schemaFor(identity, entry.path(identity).asText())
                        .orElseGet(() -> LocalizableSchema.text()
                                .format(fallbackFormat)
                                .policy(LocalizableSchema.Policy.REPLACE)
                                .build());
                fields.put(
                        key,
                        new LocalizableField(
                                key, pointer + "/" + index + "/" + escape(valueField), schema.format(), schema, group));
            }
        }
        duplicates.forEach(fields::remove);
        return List.copyOf(fields.values());
    }

    /**
     * Finds a source-owned definition by exact identity; no name or value heuristics are applied.
     *
     * @param identityField source property identifying the entry
     * @param identityValue exact source identity, without case folding
     * @return the explicit schema, or empty when the entry uses the collection's default replacement policy
     */
    public Optional<LocalizableSchema> schemaFor(String identityField, String identityValue) {
        return definitions.stream()
                .filter(definition -> definition.identityField().equals(identityField))
                .filter(definition -> definition.identityValue().equals(identityValue))
                .map(EntryDefinition::schema)
                .findFirst();
    }

    /**
     * Semantics for an explicitly identified collection entry. Identity strings address source data; all behavior is
     * determined by the schema, never by their spelling.
     *
     * @param identityField one of the collection's declared identity attributes
     * @param identityValue exact, nonblank identity value in the source
     * @param schema text, image or link semantics for the entry's string value
     */
    public record EntryDefinition(String identityField, String identityValue, LocalizableSchema schema) {
        /**
         * Creates a definition for a string-valued entry; object, array, number and boolean schemas are rejected.
         * Structured data should instead use {@link LocalizableField#object} or {@link LocalizableField#array}.
         */
        public EntryDefinition {
            requireName(identityField);
            requireName(identityValue);
            Objects.requireNonNull(schema);
            if (schema.structured()) {
                throw new IllegalArgumentException("Collection display leaves must be strings");
            }
        }
    }

    /**
     * Builds the opaque storage key for a declared identity attribute and its exact source value. Separators are
     * escaped so identities containing slashes or tildes cannot collide.
     *
     * @param identityField one of the declared identity attributes
     * @param identityValue nonblank identity value, preserved without case folding
     * @return stable semantic key, independent of array position and language
     */
    public String fieldKey(String identityField, String identityValue) {
        if (!identityFields.contains(identityField)) {
            throw new IllegalArgumentException("Undeclared identity field");
        }
        requireName(identityValue);
        return "collection:" + escape(namespace) + "/" + escape(identityField) + "/" + escape(identityValue) + "/"
                + escape(valueField);
    }

    private String identityKey(JsonNode entry) {
        if (!entry.isObject()) {
            return null;
        }
        var identities = new ArrayList<String>();
        for (var field : identityFields) {
            var value = entry.path(field);
            if (value.isNull() || value.isMissingNode()) {
                continue;
            }
            if (!value.isTextual()) {
                return null;
            }
            if (!value.asText().isBlank()) {
                identities.add(fieldKey(field, value.asText()));
            }
        }
        return identities.size() == 1 ? identities.getFirst() : null;
    }

    private static String escape(String value) {
        return value.replace("~", "~0").replace("/", "~1");
    }

    private static void requireName(String value) {
        if (Objects.requireNonNull(value).isBlank()) {
            throw new IllegalArgumentException("Collection names and identities must not be blank");
        }
    }
}
