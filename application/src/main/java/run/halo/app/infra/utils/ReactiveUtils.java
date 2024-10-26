package run.halo.app.infra.utils;

import java.time.Duration;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Utility class for reactive.
 *
 * @author johnniang
 * @since 2.20.0
 */
public enum ReactiveUtils {
    ;

    private static final Duration DEFAULT_TIMEOUT = Duration.ofMinutes(1);

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
     * @param timeout the timeout of blocking operation
     * @return the resolved value
     */
    @Nullable
    public static Object blockReactiveValue(@Nullable Object value, @NonNull Duration timeout) {
        if (value == null) {
            return null;
        }
        Class<?> clazz = value.getClass();
        if (Mono.class.isAssignableFrom(clazz)) {
            return ((Mono<?>) value).blockOptional(timeout).orElse(null);
        }
        if (Flux.class.isAssignableFrom(clazz)) {
            return ((Flux<?>) value).collectList().block(timeout);
        }
        return value;
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
