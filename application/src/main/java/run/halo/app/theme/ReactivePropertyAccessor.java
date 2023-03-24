package run.halo.app.theme;

import java.util.List;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;
import org.springframework.expression.spel.ast.AstUtils;
import org.springframework.integration.json.JsonPropertyAccessor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

    @Override
    public Class<?>[] getSpecificTargetClasses() {
        return null;
    }

    @Override
    public boolean canRead(@NonNull EvaluationContext context, Object target, @NonNull String name)
        throws AccessException {
        if (isReactiveType(target)) {
            return true;
        }
        List<PropertyAccessor> propertyAccessors = context.getPropertyAccessors();
        for (PropertyAccessor propertyAccessor : propertyAccessors) {
            if (propertyAccessor.canRead(context, target, name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    @NonNull
    public TypedValue read(@NonNull EvaluationContext context, Object target, @NonNull String name)
        throws AccessException {
        if (target == null) {
            return TypedValue.NULL;
        }
        Object value = blockingGetForReactive(target);

        List<PropertyAccessor> propertyAccessorsToTry =
            getPropertyAccessorsToTry(value, context.getPropertyAccessors());
        for (PropertyAccessor propertyAccessor : propertyAccessorsToTry) {
            try {
                TypedValue result = propertyAccessor.read(context, value, name);
                return new TypedValue(blockingGetForReactive(result.getValue()));
            } catch (AccessException e) {
                // ignore this
            }
        }

        throw new AccessException("Cannot read property '" + name + "' from [" + value + "]");
    }

    @Nullable
    private static Object blockingGetForReactive(@Nullable Object target) {
        if (target == null) {
            return null;
        }
        Class<?> clazz = target.getClass();
        Object value = target;
        if (Mono.class.isAssignableFrom(clazz)) {
            value = ((Mono<?>) target).block();
        } else if (Flux.class.isAssignableFrom(clazz)) {
            value = ((Flux<?>) target).collectList().block();
        }
        return value;
    }

    private boolean isReactiveType(Object target) {
        if (target == null) {
            return false;
        }
        Class<?> clazz = target.getClass();
        return Mono.class.isAssignableFrom(clazz)
            || Flux.class.isAssignableFrom(clazz);
    }

    private List<PropertyAccessor> getPropertyAccessorsToTry(
        @Nullable Object contextObject, List<PropertyAccessor> propertyAccessors) {

        Class<?> targetType = (contextObject != null ? contextObject.getClass() : null);

        List<PropertyAccessor> resolvers =
            AstUtils.getPropertyAccessorsToTry(targetType, propertyAccessors);
        // remove this resolver to avoid infinite loop
        resolvers.remove(this);
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
