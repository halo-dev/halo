package run.halo.app.localization;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Stored translations offered by a provider for one resource and one language.
 *
 * <p>This is a display projection candidate, not a JSON Patch or a request to update storage. Core checks the resource
 * identity, requested or fallback language, descriptor allowlist, source fingerprints and publication states before
 * using its fields. Missing or rejected fields retain their source values or use an eligible fallback translation.
 *
 * <p>A provider should return at most one patch per resource and language, with unique field keys. Duplicate patches
 * make that language ambiguous and are ignored. Duplicate field entries cannot supply a valid translation for that
 * field or its document binding.
 *
 * <p>For example, after publishing a translated title, a provider can return the following field using the title value
 * captured when it was translated:
 *
 * <pre>{@code
 * var subject = new ResourceReference(
 *     new run.halo.app.extension.GroupVersionKind("example.com", "v1alpha1", "Article"),
 *     "first-article");
 * var title = new LocalizationPatch.Field(
 *     "title", LocalizationHash.of("Hello"), "Bonjour", LocalizationPatch.State.APPROVED);
 * var patch = new LocalizationPatch(subject, Locale.FRENCH, List.of(title));
 * }</pre>
 *
 * @param subject unchanged source resource identity
 * @param locale language of every translated value in this patch
 * @param fields translated values and their source fingerprints and states; copied into an immutable list that must not
 *     contain null elements
 * @since 2.28.0
 */
public record LocalizationPatch(ResourceReference subject, Locale locale, List<Field> fields) {
    /**
     * Creates a candidate with an immutable field list; applicability is checked by Core on projection.
     *
     * @throws NullPointerException if an argument or a list element is null
     */
    public LocalizationPatch {
        Objects.requireNonNull(subject);
        Objects.requireNonNull(locale);
        fields = List.copyOf(fields);
    }

    /**
     * A translated value associated with the source content from which it was produced.
     *
     * <p>Ordinary fields use {@link LocalizationHash#of}; document parts share the fingerprint from
     * {@link LocalizationHash#sourceFields}. Do not hash the translated value or recalculate the fingerprint against
     * newer source content to make an outdated translation appear current. The record carries provider data; Core
     * validates it before projection.
     *
     * @param key semantic field key from the resource descriptor
     * @param sourceHash fingerprint captured from the untranslated source content
     * @param value translated display value; an empty string is valid, while null is ignored
     * @param state publication state; only {@link State#APPROVED} is eligible for projection
     */
    public record Field(String key, String sourceHash, String value, State state) {}

    /**
     * Publication eligibility of an individual translated field, not the status of a translation task or the
     * publication status of the source resource.
     *
     * @since 2.28.0
     */
    public enum State {
        /**
         * A saved draft, pending suggestion or otherwise unpublished value. Core does not expose it in a localized
         * public response or search projection.
         */
        DRAFT,

        /**
         * A value the provider has made eligible for public display, including publication performed by an automatic
         * workflow. This does not require manual review and is not sufficient alone: the source fingerprint, field
         * allowlist and document pairing must also be valid.
         */
        APPROVED
    }
}
