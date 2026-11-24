package run.halo.app.localization;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Codec and projection validation for explicitly declared localization values.
 *
 * <p>Text and reference fields use plain strings on the translation wire. Structured values use JSON, selected by the
 * schema rather than by inspecting string prefixes. A structured source must be encoded with this same codec before its
 * fingerprint is computed. JSON null, empty arrays and empty objects remain distinct.
 *
 * <p>Projection never mutates source nodes. Undeclared object members and preserved members cannot be changed by a
 * translation. Array edits use the declared identity, or the current source position when structure is fixed. Public
 * callers must additionally verify the source fingerprint before applying a candidate.
 *
 * @since 2.28.0
 */
public final class LocalizableValues {
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
            .enable(com.fasterxml.jackson.core.JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
    private static final int MAX_DEPTH = 12;
    private static final int MAX_ITEMS = 1000;
    private static final int MAX_LENGTH = 500_000;
    private static final String VALUE_TOO_LARGE = "Localization value is too large";
    private static final String STRUCTURE_TOO_DEEP = "Localization structure is too deep";

    private LocalizableValues() {}

    /**
     * Encodes a source value for translation storage and fingerprinting. Numbers, booleans, objects and arrays retain
     * their JSON types. Scalar textual fields use plain strings and normalize a missing or null source to an empty
     * string. A missing structured value is rejected rather than silently replaced with an empty container.
     *
     * @param schema source-owned value declaration
     * @param value original JSON value
     * @return the plain-string or JSON representation specified by the schema
     * @throws IllegalArgumentException if the value has an incompatible type or exceeds the structural limits
     */
    public static String encode(LocalizableSchema schema, JsonNode value) {
        Objects.requireNonNull(schema);
        Objects.requireNonNull(value);
        if (schema.structured()) {
            require(!value.isMissingNode(), "Missing structured value");
            validateShape(schema, value, 0);
            validateLimits(value, 0);
            var encoded = value.toString();
            require(encoded.length() <= MAX_LENGTH, VALUE_TOO_LARGE);
            return encoded;
        }
        if (value.isMissingNode() || value.isNull()) {
            return "";
        }
        checkType(schema, value);
        return value.asText();
    }

    /**
     * Decodes only the representation permitted by the schema. For example, {@code "[example]"} remains ordinary text
     * under a text schema; an array schema requires actual JSON array syntax. Nested declared types, depth and size are
     * checked before an editor or translation engine traverses the result.
     *
     * @param schema source-owned value declaration
     * @param value encoded candidate or source value
     * @return an isolated JSON node matching the declared representation
     * @throws IllegalArgumentException for invalid types, oversized values, duplicate JSON properties or malformed JSON
     */
    public static JsonNode decode(LocalizableSchema schema, String value) {
        require(value != null && value.length() <= MAX_LENGTH, "Invalid localization value length");
        if (!schema.structured()) {
            var text = JsonNodeFactory.instance.textNode(value);
            checkType(schema, text);
            return text;
        }
        try {
            var parsed = MAPPER.readTree(value);
            require(parsed != null, "Missing JSON value");
            validateShape(schema, parsed, 0);
            validateLimits(parsed, 0);
            return parsed;
        } catch (java.io.IOException error) {
            throw new IllegalArgumentException("Invalid structured localization value", error);
        }
    }

    /**
     * Checks an encoded candidate against its original source, including preserved members and array structure. This
     * does not mutate either input or verify publication state, permissions or a stored source fingerprint.
     *
     * @param schema source-owned value declaration
     * @param source original value encoded by {@link #encode}
     * @param candidate proposed localized value in the same representation
     * @throws IllegalArgumentException if the candidate violates the declaration or changes source-owned values
     */
    public static void validate(LocalizableSchema schema, String source, String candidate) {
        project(schema, decode(schema, source), decode(schema, candidate));
    }

    /**
     * Merges a candidate into an isolated source copy and validates the resulting value. Omitted object members retain
     * their source values. Undeclared and preserved members cannot be overwritten; required properties must be valid in
     * the final merged result. An independent array may match reordered objects only by its explicitly declared
     * identity when those objects contain source-owned members.
     *
     * <pre>{@code
     * var schema = LocalizableSchema.object()
     *     .property("heading", LocalizableSchema.text().required(true))
     *     .property("enabled", LocalizableSchema.bool())
     *     .build();
     * var source = LocalizableValues.decode(schema, "{\"heading\":\"Hello\",\"enabled\":true}");
     * var candidate = LocalizableValues.decode(schema, "{\"heading\":\"Bonjour\"}");
     * var localized = LocalizableValues.project(schema, source, candidate);
     * // localized still contains enabled: true; source is unchanged.
     * }</pre>
     *
     * @param schema source-owned value declaration
     * @param source original JSON value, never a previously localized copy
     * @param candidate proposed localized JSON value
     * @return an isolated merged value; no caller-supplied node is mutated
     * @throws IllegalArgumentException if the declaration, value limits or preservation rules are violated
     */
    public static JsonNode project(LocalizableSchema schema, JsonNode source, JsonNode candidate) {
        validateLimits(source, 0);
        validateLimits(candidate, 0);
        if (!source.isMissingNode()) {
            validateShape(schema, source, 0);
        }
        validateShape(schema, candidate, 0);
        var result = project(schema, source, candidate, 0);
        validateRequired(schema, result);
        return result;
    }

    private static void validateRequired(LocalizableSchema schema, JsonNode value) {
        if (value.isMissingNode() || value.isNull()) {
            require(!schema.required(), "Required value is missing or null");
            return;
        }
        if (value.isTextual()) {
            require(!schema.required() || !value.asText().isBlank(), "Required text is blank");
        }
        if (schema.type() == LocalizableSchema.Type.OBJECT) {
            schema.properties().forEach((name, child) -> validateRequired(child, value.path(name)));
        }
        if (schema.type() == LocalizableSchema.Type.ARRAY) {
            value.forEach(item -> validateRequired(schema.items(), item));
        }
    }

    private static void validateShape(LocalizableSchema schema, JsonNode value, int depth) {
        require(depth <= MAX_DEPTH, STRUCTURE_TOO_DEEP);
        checkType(schema, value);
        if (value.isNull()) {
            return;
        }
        if (schema.type() == LocalizableSchema.Type.OBJECT) {
            for (var entry : schema.properties().entrySet()) {
                var child = value.path(entry.getKey());
                if (!child.isMissingNode()) {
                    validateShape(entry.getValue(), child, depth + 1);
                }
            }
        }
        if (schema.type() == LocalizableSchema.Type.ARRAY) {
            for (var child : value) {
                validateShape(schema.items(), child, depth + 1);
            }
        }
    }

    private static void validateLimits(JsonNode value, int depth) {
        validateLimits(value, depth, new long[1]);
    }

    private static void validateLimits(JsonNode value, int depth, long[] length) {
        require(depth <= MAX_DEPTH, STRUCTURE_TOO_DEEP);
        length[0] += value.isValueNode() ? value.asText().length() : 0;
        require(length[0] <= MAX_LENGTH, VALUE_TOO_LARGE);
        if (value.isContainerNode()) {
            require(value.size() <= MAX_ITEMS, "Too many structured members");
            var names = value.fieldNames();
            while (names.hasNext()) {
                length[0] += names.next().length();
                require(length[0] <= MAX_LENGTH, VALUE_TOO_LARGE);
            }
            for (var child : value) {
                validateLimits(child, depth + 1, length);
            }
        }
    }

    private static JsonNode project(LocalizableSchema schema, JsonNode source, JsonNode candidate, int depth) {
        require(depth <= MAX_DEPTH, STRUCTURE_TOO_DEEP);
        if (schema.policy() == LocalizableSchema.Policy.PRESERVE) {
            require(source.equals(candidate), "Preserved value cannot be changed");
            return source.deepCopy();
        }
        checkType(schema, candidate);
        if (schema.required()) {
            require(!candidate.isNull(), "Required value is null");
            require(!candidate.isTextual() || !candidate.asText().isBlank(), "Required text is blank");
        }
        if (candidate.isNull()) {
            require(!containsProtected(source, schema), "Null would remove source-owned values");
            return candidate;
        }
        return switch (schema.type()) {
            case OBJECT -> projectObject(schema, source, candidate, depth);
            case ARRAY -> projectArray(schema, source, candidate, depth);
            default -> candidate.deepCopy();
        };
    }

    private static JsonNode projectObject(LocalizableSchema schema, JsonNode source, JsonNode candidate, int depth) {
        var result = source.isObject() ? (ObjectNode) source.deepCopy() : JsonNodeFactory.instance.objectNode();
        var names = candidate.fieldNames();
        while (names.hasNext()) {
            var name = names.next();
            if (!schema.properties().containsKey(name)) {
                require(source.path(name).equals(candidate.path(name)), "Undeclared property cannot be changed");
            }
        }
        for (var entry : schema.properties().entrySet()) {
            var name = entry.getKey();
            var child = entry.getValue();
            var proposed = candidate.path(name);
            if (proposed.isMissingNode()) {
                require(!child.required() || !source.path(name).isMissingNode(), "Required property is missing");
                continue;
            }
            result.set(name, project(child, source.path(name), proposed, depth + 1));
        }
        return result;
    }

    private static JsonNode projectArray(LocalizableSchema schema, JsonNode source, JsonNode candidate, int depth) {
        require(candidate.size() <= MAX_ITEMS, "Too many localized array items");
        var original = source.isArray() ? source : JsonNodeFactory.instance.arrayNode();
        if (!schema.independentStructure()) {
            require(candidate.size() == original.size(), "Array structure must follow the source");
        }
        var identities = arrayIdentities(schema, original);
        var seen = new HashSet<JsonNode>();
        var result = JsonNodeFactory.instance.arrayNode();
        for (int index = 0; index < candidate.size(); index++) {
            var item = candidate.get(index);
            var previous = arraySource(schema, original, identities, seen, item, index);
            if (schema.independentStructure() && schema.identityField() == null) {
                require(
                        !containsProtected(previous, schema.items()),
                        "Independent array items with source-owned values require an identity declaration");
            }
            result.add(project(schema.items(), previous, item, depth + 1));
        }
        return result;
    }

    private static Map<JsonNode, JsonNode> arrayIdentities(LocalizableSchema schema, JsonNode values) {
        var result = new HashMap<JsonNode, JsonNode>();
        if (schema.identityField() == null) {
            return result;
        }
        for (var item : values) {
            var identity = item.path(schema.identityField());
            require(validIdentity(identity), "Source array identity is missing");
            require(result.putIfAbsent(identity, item) == null, "Duplicate source array identity");
        }
        return result;
    }

    private static JsonNode arraySource(
            LocalizableSchema schema,
            JsonNode original,
            Map<JsonNode, JsonNode> identities,
            Set<JsonNode> seen,
            JsonNode item,
            int index) {
        if (schema.identityField() == null) {
            return original.path(index);
        }
        var identity = item.path(schema.identityField());
        require(validIdentity(identity), "Localized array identity is missing");
        require(seen.add(identity), "Duplicate localized array identity");
        var previous = identities.get(identity);
        if (previous == null) {
            require(schema.independentStructure(), "Localized array identity must originate in the source");
            var added = JsonNodeFactory.instance.objectNode();
            added.set(schema.identityField(), identity.deepCopy());
            return added;
        }
        if (!schema.independentStructure()) {
            require(
                    original.path(index).path(schema.identityField()).equals(identity),
                    "Array order must follow the source");
        }
        return previous;
    }

    private static boolean validIdentity(JsonNode value) {
        if (value.isNumber()) {
            return true;
        }
        return value.isTextual() && !value.asText().isBlank();
    }

    private static boolean containsProtected(JsonNode value, LocalizableSchema schema) {
        if (value.isMissingNode()) {
            return false;
        }
        if (schema.policy() == LocalizableSchema.Policy.PRESERVE) {
            return true;
        }
        if (value.isObject()) {
            var fields = value.fields();
            while (fields.hasNext()) {
                var entry = fields.next();
                var child = schema.properties().get(entry.getKey());
                if (child == null || containsProtected(entry.getValue(), child)) {
                    return true;
                }
            }
        }
        if (value.isArray()) {
            for (var item : value) {
                if (containsProtected(item, schema.items())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void checkType(LocalizableSchema schema, JsonNode value) {
        if (value.isNull()) {
            require(schema.nullable(), "Null is not allowed");
            return;
        }
        boolean valid =
                switch (schema.type()) {
                    case TEXT, IMAGE, LINK -> value.isTextual();
                    case NUMBER -> value.isNumber();
                    case BOOLEAN -> value.isBoolean();
                    case OBJECT -> value.isObject();
                    case ARRAY -> value.isArray();
                };
        require(valid, "Localization value does not match its declared type");
        if (value.isTextual()) {
            require(value.asText().length() <= MAX_LENGTH, "Text is too long");
            if (schema.type() == LocalizableSchema.Type.IMAGE || schema.type() == LocalizableSchema.Type.LINK) {
                validateReference(value.asText(), schema.type());
            }
        }
    }

    private static void validateReference(String value, LocalizableSchema.Type type) {
        if (value.isBlank()) {
            return;
        }
        require(
                value.chars().noneMatch(character -> character < 32 || character == 127 || character == '\\'),
                "Invalid reference characters");
        var uri = URI.create(value);
        if (!uri.isAbsolute()) {
            require(uri.getRawAuthority() == null, "Protocol-relative references are not supported");
            return;
        }
        var scheme = uri.getScheme().toLowerCase(java.util.Locale.ROOT);
        var allowed = type == LocalizableSchema.Type.IMAGE
                ? Set.of("http", "https")
                : Set.of("http", "https", "mailto", "tel");
        require(allowed.contains(scheme), "Unsupported reference scheme");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
