package run.halo.app.infra.utils;

import java.time.Duration;
import org.jspecify.annotations.Nullable;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

/**
 * Utility class for reactive.
 *
 * @author johnniang
 * @since 2.20.0
 */
public enum ReactiveUtils {
    ;

    /**
     * Default timeout for blocking operation.
     */
    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);

    /**
     * Resolve reactive value by blocking operation.
     *
     * @param value the normal value or reactive value
     * @return the resolved value
     */
    @Nullable
    public static Object blockReactiveValue(@Nullable Object value) {
        return blockReactiveValue(value, DEFAULT_TIMEOUT);
    }

    /**
     * Resolve reactive value by blocking operation.
     *
     * @param value the normal value or reactive value
     * @return the resolved value
     */
    @Nullable
    public static Object blockReactiveValue(@Nullable Object value, ContextView contextView) {
        return blockReactiveValue(value, contextView, DEFAULT_TIMEOUT);
    }

    /**
     * Resolve reactive value by blocking operation.
     *
     * @param value the normal value or reactive value
     * @param timeout the timeout of blocking operation
     * @return the resolved value
     */
    @Nullable
    public static Object blockReactiveValue(
        @Nullable Object value, @Nullable ContextView contextView, @NonNull Duration timeout
    ) {
        if (value == null) {
            return null;
        }
        if (contextView == null) {
            contextView = Context.empty();
        }
        Class<?> clazz = value.getClass();
        if (Mono.class.isAssignableFrom(clazz)) {
            return ((Mono<?>) value).contextWrite(contextView).blockOptional(timeout).orElse(null);
        }
        if (Flux.class.isAssignableFrom(clazz)) {
            return ((Flux<?>) value).contextWrite(contextView).collectList().block(timeout);
        }
        return value;
    }

    /**
     * Resolve reactive value by blocking operation.
     *
     * @param value the normal value or reactive value
     * @param timeout the timeout of blocking operation
     * @return the resolved value
     */
    @Nullable
    public static Object blockReactiveValue(@Nullable Object value, @NonNull Duration timeout) {
        return blockReactiveValue(value, null, timeout);
    }

    /**
     * Check if the class is a reactive type.
     *
     * @param clazz the class to check
     * @return true if the class is a reactive type, false otherwise
     */
    public static boolean isReactiveType(@NonNull Class<?> clazz) {
        return Mono.class.isAssignableFrom(clazz) || Flux.class.isAssignableFrom(clazz);
    }
}
