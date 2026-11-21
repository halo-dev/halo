package run.halo.app.localization;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Identifies the serialized, source-owned stable text or integer ID of a registered non-Extension display DTO. The
 * property cannot also be translated. Resource owners must include tenant or data-source identity in the value when
 * necessary to make it unique within their namespace and type.
 *
 * @since 2.28.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.RECORD_COMPONENT})
public @interface LocalizationId {}
