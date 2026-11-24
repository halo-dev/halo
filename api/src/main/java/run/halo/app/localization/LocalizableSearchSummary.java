package run.halo.app.localization;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a localized plain-text property as the summary used by localized search results.
 *
 * <p>The property must also declare {@link Localizable}. A resource can declare at most one search summary.
 *
 * @since 2.28.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.RECORD_COMPONENT})
public @interface LocalizableSearchSummary {}
