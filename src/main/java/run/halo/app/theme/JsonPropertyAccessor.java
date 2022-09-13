package run.halo.app.theme;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.IOException;
import java.util.AbstractList;
import java.util.Iterator;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * A SpEL {@link PropertyAccessor} that knows how to read properties from JSON objects.
 * Uses Jackson {@link JsonNode} API for nested properties access.
 * Reference from
 * <a href="https://github.com/spring-projects/spring-integration">spring-integration</a>
 *
 * @author guqing
 * @see <a href="https://github.com/spring-projects/spring-integration">spring-integration</a>
 * @since 2.0.0
 */
public class JsonPropertyAccessor implements PropertyAccessor {

    /**
     * The kind of types this can work with.
     */
    private static final Class<?>[] SUPPORTED_CLASSES = {
        String.class,
        JsonNodeWrapper.class,
        JsonNode.class
    };

    private ObjectMapper objectMapper = new ObjectMapper();

    public void setObjectMapper(ObjectMapper objectMapper) {
        Assert.notNull(objectMapper, "'objectMapper' cannot be null");
        this.objectMapper = objectMapper;
    }

    @Override
    public Class<?>[] getSpecificTargetClasses() {
        // expose internals
        return SUPPORTED_CLASSES;
    }

    @Override
    public boolean canRead(@NonNull EvaluationContext context, Object target, @NonNull String name)
        throws AccessException {
        JsonNode node;
        try {
            node = asJson(target);
        } catch (AccessException e) {
            // Cannot parse - treat as not a JSON
            return false;
        }
        Integer index = maybeIndex(name);
        if (node instanceof ArrayNode) {
            return index != null;
        }
        return true;
    }

    private JsonNode asJson(Object target) throws AccessException {
        if (target instanceof JsonNode) {
            return (JsonNode) target;
        } else if (target instanceof JsonNodeWrapper) {
            return ((JsonNodeWrapper<?>) target).getRealNode();
        } else if (target instanceof String) {
            try {
                return this.objectMapper.readTree((String) target);
            } catch (JsonProcessingException e) {
                throw new AccessException("Exception while trying to deserialize String", e);
            }
        } else {
            throw new IllegalStateException("Can't happen. Check SUPPORTED_CLASSES");
        }
    }

    /**
     * Return an integer if the String property name can be parsed as an int, or null otherwise.
     */
    private static Integer maybeIndex(String name) {
        try {
            return Integer.valueOf(name);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @NonNull
    @Override
    public TypedValue read(@NonNull EvaluationContext context, @Nullable Object target,
        @NonNull String name)
        throws AccessException {
        JsonNode node = asJson(target);
        Integer index = maybeIndex(name);
        if (index != null && node.has(index)) {
            return typedValue(node.get(index));
        } else {
            return typedValue(node.get(name));
        }
    }

    @Override
    public boolean canWrite(@NonNull EvaluationContext context, Object target,
        @NonNull String name) {
        return false;
    }

    @Override
    public void write(@NonNull EvaluationContext context, Object target, @NonNull String name,
        Object newValue) {
        throw new UnsupportedOperationException("Write is not supported");
    }

    private static TypedValue typedValue(JsonNode json) throws AccessException {
        if (json == null) {
            return TypedValue.NULL;
        } else if (json.isValueNode()) {
            return new TypedValue(getValue(json));
        }
        return new TypedValue(wrap(json));
    }

    private static Object getValue(JsonNode json) throws AccessException {
        if (json.isTextual()) {
            return json.textValue();
        } else if (json.isNumber()) {
            return json.numberValue();
        } else if (json.isBoolean()) {
            return json.asBoolean();
        } else if (json.isNull()) {
            return null;
        } else if (json.isBinary()) {
            try {
                return json.binaryValue();
            } catch (IOException e) {
                throw new AccessException(
                    "Can not get content of binary value: " + json, e);
            }
        }
        throw new IllegalArgumentException("Json is not ValueNode.");
    }

    static Object wrap(JsonNode json) throws AccessException {
        if (json == null) {
            return null;
        } else if (json instanceof ArrayNode) {
            return new ArrayNodeAsList((ArrayNode) json);
        } else if (json.isValueNode()) {
            return getValue(json);
        } else {
            return new ComparableJsonNode(json);
        }
    }

    interface JsonNodeWrapper<T> extends Comparable<T> {

        String toString();

        JsonNode getRealNode();

    }

    static class ComparableJsonNode implements JsonNodeWrapper<ComparableJsonNode> {

        private final JsonNode delegate;

        ComparableJsonNode(JsonNode delegate) {
            this.delegate = delegate;
        }

        @Override
        public JsonNode getRealNode() {
            return this.delegate;
        }

        @Override
        public String toString() {
            return this.delegate.toString();
        }

        @Override
        public int compareTo(ComparableJsonNode o) {
            return this.delegate.equals(o.delegate) ? 0 : 1;
        }

    }

    /**
     * An {@link AbstractList} implementation around {@link ArrayNode} with
     * {@link JsonNodeWrapper} aspect.
     *
     * @since 2.0.0
     */
    static class ArrayNodeAsList extends AbstractList<Object> implements JsonNodeWrapper<Object> {

        private final ArrayNode delegate;

        ArrayNodeAsList(ArrayNode node) {
            this.delegate = node;
        }

        @Override
        public JsonNode getRealNode() {
            return this.delegate;
        }

        @Override
        public String toString() {
            return this.delegate.toString();
        }

        @Override
        public Object get(int index) {
            // negative index - get from the end of list
            int i = index < 0 ? this.delegate.size() + index : index;
            try {
                return wrap(this.delegate.get(i));
            } catch (AccessException ex) {
                throw new IllegalArgumentException(ex);
            }
        }

        @Override
        public int size() {
            return this.delegate.size();
        }

        @Override
        public Iterator<Object> iterator() {

            return new Iterator<Object>() {

                private final Iterator<JsonNode> it = ArrayNodeAsList.this.delegate.iterator();

                @Override
                public boolean hasNext() {
                    return this.it.hasNext();
                }

                @Override
                public Object next() {
                    try {
                        return wrap(this.it.next());
                    } catch (AccessException e) {
                        throw new IllegalArgumentException(e);
                    }
                }

            };
        }

        @Override
        public int compareTo(@NonNull Object o) {
            if (o instanceof JsonNodeWrapper<?>) {
                return this.delegate.equals(((JsonNodeWrapper<?>) o).getRealNode()) ? 0 : 1;
            }
            return this.delegate.equals(o) ? 0 : 1;
        }

    }

}