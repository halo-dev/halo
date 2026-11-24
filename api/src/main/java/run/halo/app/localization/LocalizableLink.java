package run.halo.app.localization;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a serialized string property whose value is a public URL that should receive language-aware URL rewriting.
 *
 * <p>A link target is not translatable text. The property must not also use {@link Localizable} or contain a translated
 * slug. The declared value is passed to {@link ResourceLocalizationProvider#localizeUrl(String, LocalizationContext)}
 * by Core projection; it is never sent to a translation engine or stored in a translation record.
 *
 * <pre>{@code
 * @LocalizableLink
 * private String permalink;
 * }</pre>
 *
 * @since 2.28.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.RECORD_COMPONENT})
public @interface LocalizableLink {}
