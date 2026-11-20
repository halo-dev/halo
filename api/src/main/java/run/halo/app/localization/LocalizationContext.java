package run.halo.app.localization;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import reactor.core.publisher.Mono;

/**
 * Immutable language and consumer information for a localized read.
 *
 * <p>Pass this value directly to {@link ResourceLocalizationService}, or store it in Reactor's subscriber context under
 * {@code LocalizationContext.class} for consumers using {@link #current()}. Constructing the record alone does not
 * activate localization. The context is not a thread-local value and must be attached to the reactive chain that
 * performs the read.
 *
 * <p>For example, an English public-page subscription can expose its requested language as follows:
 *
 * <pre>{@code
 * var localization = new LocalizationContext(
 *     Locale.SIMPLIFIED_CHINESE, Locale.ENGLISH, List.of(), LocalizationScope.PUBLIC_PAGE);
 * Mono<Locale> requestedLanguage = LocalizationContext.current()
 *     .map(LocalizationContext::targetLocale)
 *     .contextWrite(context -> context.put(LocalizationContext.class, localization));
 * }</pre>
 *
 * <p>The target language is tried before the ordered fallbacks. Core stops trying translation languages at the source
 * language and retains original values when no valid translation is found. This record does not verify that a language
 * is enabled or that the caller may read a resource.
 *
 * @param sourceLocale language of the original resource values, not the Console interface language
 * @param targetLocale language requested by the consumer; equal to the source language disables projection
 * @param fallbackLocales languages to try after the target, in priority order; use an empty list for no translated
 *     fallback. The list is copied and must not contain null elements
 * @param scope purpose of the read; does not replace access-control checks
 * @since 2.28.0
 */
public record LocalizationContext(
        Locale sourceLocale, Locale targetLocale, List<Locale> fallbackLocales, LocalizationScope scope) {
    /**
     * Creates a context with an immutable copy of the fallback language list.
     *
     * @throws NullPointerException if an argument or a fallback language is null
     */
    public LocalizationContext {
        Objects.requireNonNull(sourceLocale);
        Objects.requireNonNull(targetLocale);
        Objects.requireNonNull(scope);
        fallbackLocales = List.copyOf(fallbackLocales);
    }

    /**
     * Determines whether the scope and target language permit an attempt at localization.
     *
     * <p>A true result does not mean that a provider, descriptor or published translation exists. It also does not
     * authorize access to the source resource.
     *
     * @return false for {@link LocalizationScope#CONSOLE}, {@link LocalizationScope#INTERNAL} or an unchanged source
     *     language; true otherwise
     */
    public boolean enabled() {
        if (scope == LocalizationScope.CONSOLE) {
            return false;
        }
        if (scope == LocalizationScope.INTERNAL) {
            return false;
        }
        return !sourceLocale.equals(targetLocale);
    }

    /**
     * Gets the localization context of the current reactive subscription.
     *
     * <p>The lookup is deferred until subscription. An absent context does not cause a default language to be inferred;
     * callers should retain source values in that case.
     *
     * @return the context, or an empty publisher when localization was not requested
     */
    public static Mono<LocalizationContext> current() {
        return Mono.deferContextual(context -> Mono.justOrEmpty(context.getOrEmpty(LocalizationContext.class)));
    }
}
