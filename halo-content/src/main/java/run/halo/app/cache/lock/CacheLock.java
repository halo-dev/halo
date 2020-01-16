package run.halo.app.cache.lock;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * Cache lock annotation.
 *
 * @author johnniang
 * @date 3/28/19
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface CacheLock {

    /**
     * Cache prefix, default is ""
     *
     * @return cache prefix
     */
    @AliasFor("value")
    String prefix() default "";

    /**
     * Alias of prefix, default is ""
     *
     * @return alias of prefix
     */
    @AliasFor("prefix")
    String value() default "";

    /**
     * Expired time, default is 5.
     *
     * @return expired time
     */
    long expired() default 5;

    /**
     * Time unit, default is TimeUnit.SECONDS.
     *
     * @return time unit
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * Delimiter, default is ':'
     *
     * @return delimiter
     */
    String delimiter() default ":";

    /**
     * Whether delete cache after method invocation.
     *
     * @return true if delete cache after method invocation; false otherwise
     */
    boolean autoDelete() default true;

    /**
     * Whether trace the request info.
     *
     * @return true if trace the request info; false otherwise
     */
    boolean traceRequest() default false;
}
