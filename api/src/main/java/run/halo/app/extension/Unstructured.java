package run.halo.app.extension;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.core.util.Json;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.springframework.lang.NonNull;

/**
 * Unstructured is a generic Extension, which wraps ObjectNode to maintain the Extension data, like
 * apiVersion, kind, metadata and others.
 *
 * @author johnniang
 */
@JsonSerialize(using = Unstructured.UnstructuredSerializer.class)
@JsonDeserialize(using = Unstructured.UnstructuredDeserializer.class)
@SuppressWarnings("rawtypes")
public class Unstructured implements Extension {

    @SuppressWarnings("deprecation")
    public static final ObjectMapper OBJECT_MAPPER = Json.mapper()
        // We don't want to change the default mapper
        // so we copy a new one and configure it
        .copy()
        .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);

    private final Map data;

    public Unstructured() {
        this(new HashMap());
    }

    public Unstructured(Map data) {
        this.data = data;
    }

    public Map getData() {
        return Collections.unmodifiableMap(data);
    }

    @Override
    public String getApiVersion() {
        return (String) data.get("apiVersion");
    }

    @Override
    public String getKind() {
        return (String) data.get("kind");
    }

    @Override
    public MetadataOperator getMetadata() {
        return getNestedMap(data, "metadata")
            .map(UnstructuredMetadata::new)
            .orElse(null);
    }

    static class UnstructuredMetadata implements MetadataOperator {

        @NonNull
        private final Map<String, Object> metadata;

        UnstructuredMetadata(@NonNull Map<String, Object> metadata) {
            this.metadata = metadata;
        }

        @Override
        public String getName() {
            return (String) getNestedValue(metadata, "name").orElse(null);
        }

        @Override
        public String getGenerateName() {
            return (String) getNestedValue(metadata, "generateName").orElse(null);
        }

        @Override
        public Map<String, String> getLabels() {
            return getNestedStringStringMap(metadata, "labels").orElse(null);
        }

        @Override
        public Map<String, String> getAnnotations() {
            return getNestedStringStringMap(metadata, "annotations").orElse(null);
        }

        @Override
        public Long getVersion() {
            return getNestedLong(metadata, "version").orElse(null);
        }

        @Override
        public Instant getCreationTimestamp() {
            return getNestedInstant(metadata, "creationTimestamp").orElse(null);
        }

        @Override
        public Instant getDeletionTimestamp() {
            return getNestedInstant(metadata, "deletionTimestamp").orElse(null);
        }

        @Override
        public Set<String> getFinalizers() {
            return getNestedStringSet(metadata, "finalizers").orElse(null);
        }

        @Override
        public void setName(String name) {
            setNestedValue(metadata, name, "name");
        }

        @Override
        public void setGenerateName(String generateName) {
            setNestedValue(metadata, generateName, "generateName");
        }

        @Override
        public void setLabels(Map<String, String> labels) {
            setNestedValue(metadata, labels, "labels");
        }

        @Override
        public void setAnnotations(Map<String, String> annotations) {
            setNestedValue(metadata, annotations, "annotations");
        }

        @Override
        public void setVersion(Long version) {
            setNestedValue(metadata, version, "version");
        }

        @Override
        public void setCreationTimestamp(Instant creationTimestamp) {
            setNestedValue(metadata, creationTimestamp, "creationTimestamp");
        }

        @Override
        public void setDeletionTimestamp(Instant deletionTimestamp) {
            setNestedValue(metadata, deletionTimestamp, "deletionTimestamp");
        }

        @Override
        public void setFinalizers(Set<String> finalizers) {
            setNestedValue(metadata, finalizers, "finalizers");
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            var that = (UnstructuredMetadata) o;
            return Objects.equals(metadata, that.metadata);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(metadata);
        }

    }

    @Override
    public void setApiVersion(String apiVersion) {
        setNestedValue(data, apiVersion, "apiVersion");
    }

    @Override
    public void setKind(String kind) {
        setNestedValue(data, kind, "kind");
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setMetadata(MetadataOperator metadata) {
        Map metadataMap = OBJECT_MAPPER.convertValue(metadata, Map.class);
        data.put("metadata", metadataMap);
    }

    public static Optional<Object> getNestedValue(Map map, String... fields) {
        if (fields == null || fields.length == 0) {
            return Optional.of(map);
        }
        Map tempMap = map;
        for (int i = 0; i < fields.length - 1; i++) {
            Object value = tempMap.get(fields[i]);
            if (!(value instanceof Map)) {
                return Optional.empty();
            }
            tempMap = (Map<?, ?>) value;
        }
        return Optional.ofNullable(tempMap.get(fields[fields.length - 1]));
    }

    @SuppressWarnings("unchecked")
    public static Optional<List<String>> getNestedStringList(Map map, String... fields) {
        return getNestedValue(map, fields).map(value -> (List<String>) value);
    }

    public static Optional<Set<String>> getNestedStringSet(Map map, String... fields) {
        return getNestedValue(map, fields).map(value -> {
            if (value instanceof Collection collection) {
                return new LinkedHashSet<>(collection);
            }
            throw new IllegalArgumentException(
                "Incorrect value type: " + value.getClass() + ", expected: " + Set.class);
        });
    }

    @SuppressWarnings("unchecked")
    public static void setNestedValue(Map map, Object value, String... fields) {
        if (fields == null || fields.length == 0) {
            // do nothing when no fields provided
            return;
        }
        var prevFields = Arrays.stream(fields, 0, fields.length - 1)
            .toArray(String[]::new);
        getNestedMap(map, prevFields).ifPresent(m -> {
            var lastField = fields[fields.length - 1];
            m.put(lastField, value);
        });
    }

    public static Optional<Map> getNestedMap(Map map, String... fields) {
        return getNestedValue(map, fields).map(value -> (Map) value);
    }

    @SuppressWarnings("unchecked")
    public static Optional<Map<String, String>> getNestedStringStringMap(Map map,
        String... fields) {
        return getNestedValue(map, fields)
            .map(labelsObj -> (Map<String, String>) labelsObj);
    }

    public static Optional<Instant> getNestedInstant(Map map, String... fields) {
        return getNestedValue(map, fields)
            .map(instantValue -> {
                if (instantValue instanceof Instant instant) {
                    return instant;
                }
                return Instant.parse(instantValue.toString());
            });

    }

    public static Optional<Long> getNestedLong(Map map, String... fields) {
        return getNestedValue(map, fields)
            .map(longObj -> {
                if (longObj instanceof Long l) {
                    return l;
                }
                return Long.valueOf(longObj.toString());
            });
    }

    public static class UnstructuredSerializer extends JsonSerializer<Unstructured> {

        @Override
        public void serialize(Unstructured value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
            gen.writeObject(value.data);
        }

    }

    public static class UnstructuredDeserializer extends JsonDeserializer<Unstructured> {

        @Override
        public Unstructured deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException {
            Map data = p.getCodec().readValue(p, Map.class);
            return new Unstructured(data);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Unstructured that = (Unstructured) o;
        return Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }
}
