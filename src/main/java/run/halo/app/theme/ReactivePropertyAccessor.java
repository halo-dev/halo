package run.halo.app.theme;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;
import org.springframework.integration.json.JsonPropertyAccessor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.infra.utils.JsonUtils;

/**
 * A SpEL PropertyAccessor that knows how to read properties from {@link Mono} or {@link Flux}
 * object. It first converts the target to the actual value and then calls other
 * {@link PropertyAccessor}s to parse the result, If it still cannot be resolved,
 * {@link JsonPropertyAccessor} will be used to resolve finally.
 *
 * @author guqing
 * @since 2.0.0
 */
public class ReactivePropertyAccessor implements PropertyAccessor {
    private static final Class<?>[] SUPPORTED_CLASSES = {
        Mono.class,
        Flux.class
    };
    private final JsonPropertyAccessor jsonPropertyAccessor = new JsonPropertyAccessor();

    @Override
    public Class<?>[] getSpecificTargetClasses() {
        return SUPPORTED_CLASSES;
    }

    @Override
    public boolean canRead(@NonNull EvaluationContext context, Object target, @NonNull String name)
        throws AccessException {
        if (target == null) {
            return false;
        }
        return Mono.class.isAssignableFrom(target.getClass())
            || Flux.class.isAssignableFrom(target.getClass());
    }

    @Override
    @NonNull
    public TypedValue read(@NonNull EvaluationContext context, Object target, @NonNull String name)
        throws AccessException {
        if (target == null) {
            return TypedValue.NULL;
        }
        Class<?> clazz = target.getClass();
        Object value = null;
        if (Mono.class.isAssignableFrom(clazz)) {
            value = ((Mono<?>) target).block();
        } else if (Flux.class.isAssignableFrom(clazz)) {
            value = ((Flux<?>) target).collectList().block();
        }

        if (value == null) {
            return TypedValue.NULL;
        }

        List<PropertyAccessor> propertyAccessorsToTry =
            getPropertyAccessorsToTry(value, context.getPropertyAccessors());
        for (PropertyAccessor propertyAccessor : propertyAccessorsToTry) {
            try {
                return propertyAccessor.read(context, target, name);
            } catch (AccessException e) {
                // ignore
            }
        }
        JsonNode jsonNode = JsonUtils.DEFAULT_JSON_MAPPER.convertValue(value, JsonNode.class);
        return jsonPropertyAccessor.read(context, jsonNode, name);
    }

    private List<PropertyAccessor> getPropertyAccessorsToTry(
        @Nullable Object contextObject, List<PropertyAccessor> propertyAccessors) {

        Class<?> targetType = (contextObject != null ? contextObject.getClass() : null);

        List<PropertyAccessor> specificAccessors = new ArrayList<>();
        List<PropertyAccessor> generalAccessors = new ArrayList<>();
        for (PropertyAccessor resolver : propertyAccessors) {
            Class<?>[] targets = resolver.getSpecificTargetClasses();
            if (targets == null) {
                // generic resolver that says it can be used for any type
                generalAccessors.add(resolver);
            } else if (targetType != null) {
                for (Class<?> clazz : targets) {
                    if (clazz == targetType) {
                        specificAccessors.add(resolver);
                        break;
                    } else if (clazz.isAssignableFrom(targetType)) {
                        generalAccessors.add(resolver);
                    }
                }
            }
        }
        List<PropertyAccessor> resolvers = new ArrayList<>(specificAccessors);
        generalAccessors.removeAll(specificAccessors);
        resolvers.addAll(generalAccessors);
        return resolvers;
    }

    @Override
    public boolean canWrite(@NonNull EvaluationContext context, Object target, @NonNull String name)
        throws AccessException {
        return false;
    }

    @Override
    public void write(@NonNull EvaluationContext context, Object target, @NonNull String name,
        Object newValue)
        throws AccessException {
        throw new UnsupportedOperationException("Write is not supported");
    }
}
