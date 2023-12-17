package run.halo.app.theme.finders;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.stereotype.Service;

/**
 * Template model data finder for theme.
 *
 * @author guqing
 * @since 2.0.0
 */
@Service
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Finder {

    /**
     * The name of the theme model variable.
     *
     * @return variable name, class simple name if not specified
     */
    String value() default "";
}