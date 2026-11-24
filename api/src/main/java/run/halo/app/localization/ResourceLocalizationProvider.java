package run.halo.app.localization;

import java.util.Locale;
import java.util.Set;
import org.pf4j.ExtensionPoint;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ConfigMap;

/**
 * Supplies stored translations and localized public links to Core's projection service.
 *
 * <p>This is a {@code SINGLETON} extension point: Core uses the enabled provider. Resource owners declare translatable
 * fields through {@link ResourceLocalizationDescriptor}; the provider owns translation storage. It must never start AI
 * translation, publish content or persist a projection in response to a public read. All storage access must remain
 * non-blocking.
 *
 * <p>Return the source fingerprint captured when a translation was produced, not a newly computed fingerprint of the
 * latest source. Core checks identity, locale, allowed fields, publication state and source fingerprints before
 * applying a resource patch. A {@link LocalizationScope} identifies the consumer; it does not grant access to protected
 * source content.
 *
 * @see ResourceLocalizationService
 * @see LocalizationPatch
 * @since 2.28.0
 */
public interface ResourceLocalizationProvider extends ExtensionPoint {
    /**
     * Resolves an explicitly requested language for a public API request. Providers validate configured and enabled
     * languages and supply their source/fallback configuration. An empty result retains source values. This does not
     * authorize the request or perform translation work.
     *
     * @param requested explicitly requested language
     * @param scope consumer scope; the public API filter requests {@link LocalizationScope#PUBLIC_API}
     * @return configured context or an empty publisher when unavailable
     * @see PublicApiLocalizationFilter
     */
    default Mono<LocalizationContext> resolveContext(Locale requested, LocalizationScope scope) {
        return Mono.empty();
    }

    /**
     * Resolves stored translations for a batch of resource identities.
     *
     * <p>Include available translations for {@link LocalizationContext#targetLocale()} and its ordered fallback
     * languages. Emit at most one patch per resource and language, with at most one value per semantic field key; Core
     * ignores ambiguous duplicates. The emission order does not determine language preference. Missing translations are
     * omitted, and an empty input should produce an empty publisher.
     *
     * <p>Use {@link LocalizationHash#sourceFields(java.util.Map, java.util.List)} when storing source fingerprints for
     * document translations, so raw content and rendered HTML share a revision. Only fields marked
     * {@link LocalizationPatch.State#APPROVED} can be projected.
     *
     * @param subjects unique source identities requested by the consumer
     * @param context requested language, ordered fallbacks and consumer scope
     * @return available patches for the requested resources, or an empty publisher if none exist
     */
    Flux<LocalizationPatch> resolveAll(Set<ResourceReference> subjects, LocalizationContext context);

    /**
     * Projects explicitly selected display settings for a public request.
     *
     * <p>The supplied configuration is an isolated copy. Use its metadata to identify the source settings, preserve
     * unselected values and replace only display values with valid published translations. The provider is responsible
     * for validating these setting translations; they are not checked as {@link LocalizationPatch} fields. Never
     * persist the result or generate translations during this call.
     *
     * <p>Core invokes this hook for {@link LocalizationScope#PUBLIC_PAGE} and {@link LocalizationScope#PUBLIC_API}, and
     * retains the source metadata when applying the returned data. The default implementation returns the supplied
     * configuration unchanged.
     *
     * @param source configuration copy, identified by its original metadata
     * @param context public request language and consumer scope
     * @return display configuration, or an empty publisher to retain the source
     * @since 2.28.0
     */
    default Mono<ConfigMap> localizeSettings(ConfigMap source, LocalizationContext context) {
        return Mono.just(source);
    }

    /**
     * Maps a public content link to the requested language.
     *
     * <p>Implementations must be pure and non-blocking. Rewrite only supported links belonging to this site, preserving
     * query parameters and fragments. External, administrative and unsupported links must remain unchanged; resource
     * identities must never be modified. This hook maps a display link, and does not register routes or authorize the
     * destination. The default implementation returns the original link.
     *
     * @param url original public link
     * @param context requested language and consumer scope
     * @return the localized link, the original link when unchanged, or null to retain the original
     */
    default String localizeUrl(String url, LocalizationContext context) {
        return url;
    }
}
