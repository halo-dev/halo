package run.halo.app.search;

import org.pf4j.ExtensionPoint;
import reactor.core.publisher.Flux;

/**
 * Supplies stored translations as language-specific variants of an original search document.
 *
 * <p>This is a {@code SINGLETON} extension point. Core invokes the enabled provider during indexing only when the
 * search engine supports {@linkplain SearchEngine#supportsLocalizedDocuments() localized documents}. The provider
 * supplies display values; Core assigns each variant its own index ID and retains the original document's resource
 * identity, owner, categories, tags and visibility flags.
 *
 * <p>Implementations must read existing, publicly available translations without modifying the supplied document or
 * invoking a translation service. Return an empty publisher for unsupported resources or missing translations. Core
 * still indexes the original document when no variants are available or variant resolution fails.
 *
 * @see HaloDocument#getSourceId()
 * @since 2.28.0
 */
public interface HaloDocumentVariantProvider extends ExtensionPoint {
    /**
     * Resolves the currently available language variants of one original document.
     *
     * <p>Emit at most 100 variants with distinct language tags after normalization, such as {@code en} and
     * {@code zh-Hant}. Tags must be valid BCP 47 tags and must not resolve to {@code und}. A duplicate locale, invalid
     * variant or failed publisher causes Core to index only the original document for this update.
     *
     * <p>Resolution must be non-blocking and must not generate translations. Core may retry the subscription; repeated
     * subscriptions must be safe and must not create translation tasks or other persistent side effects.
     *
     * @param source original search document; its identity, content and visibility must not be modified
     * @return finite publisher of translated display values; empty when no variants are available
     */
    Flux<Variant> variants(HaloDocument source);

    /**
     * Searchable display values for one content language, without resource identity or access-control fields.
     *
     * <p>For example, after loading a published English translation for a document:
     *
     * <pre>{@code
     * var variant = new HaloDocumentVariantProvider.Variant(
     *     "en", "Welcome", "Getting started", "Welcome to the site.", "/en/archives/welcome");
     * }</pre>
     *
     * <p>The provider is responsible for selecting published content and producing searchable plain text. Core
     * preserves the source document's visibility; a variant cannot make a private source public.
     *
     * @param locale BCP 47 language tag of the variant, for example {@code en} or {@code zh-Hant}
     * @param title translated document title; must not be null
     * @param description translated summary, or null when unavailable
     * @param content searchable translated body without HTML tags; must not be null
     * @param permalink public link to this language version; must not be null
     * @since 2.28.0
     */
    record Variant(String locale, String title, String description, String content, String permalink) {}
}
