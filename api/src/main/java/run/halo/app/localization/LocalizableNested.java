package run.halo.app.localization;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Traverses a serialized object to discover separately keyed {@link Localizable} fields. This does not make the object
 * itself translatable. Use {@code @Localizable(type = OBJECT)} for one structured translation unit.
 *
 * <pre>{@code
 * @LocalizableNested
 * private Spec spec;
 * }</pre>
 *
 * @since 2.28.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.RECORD_COMPONENT})
public @interface LocalizableNested {}
