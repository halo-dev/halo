package run.halo.app.localization;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Objects;

/**
 * Declares one display field that a resource owner permits to be translated.
 *
 * <p>The semantic key identifies the field in translation storage and remains separate from its location in the
 * resource. A pointer addresses a textual display leaf under {@code /spec/} or {@code /status/}; it must not be used to
 * expose identifiers, credentials or operational settings. A null pointer denotes content resolved separately by
 * {@link ResourceLocalizationDescriptor#resolveFields}, such as a released snapshot's body.
 *
 * <p>For example, a title stored on the resource and a separately resolved Markdown source can be declared as follows.
 * A raw document field also needs a {@link LocalizableDocument} binding:
 *
 * <pre>{@code
 * var title = new LocalizableField("title", "/spec/title", LocalizableField.Format.PLAIN_TEXT);
 * var raw = new LocalizableField("raw", null, LocalizableField.Format.RAW_CONTENT);
 * }</pre>
 *
 * @param key nonblank semantic key, unique within the descriptor and stable across languages
 * @param pointer JSON pointer to a display leaf under spec or status, or null for aggregated content
 * @param format representation of the field value; does not by itself select an editor or renderer
 * @param fallbackPointer alternate display leaf when the primary value is not nonblank text, or null when no fallback
 *     is needed; requires a non-null primary pointer
 * @since 2.28.0
 */
public record LocalizableField(String key, String pointer, Format format, String fallbackPointer) {
    /**
     * Declares a field without an alternate display leaf.
     *
     * @param key stable semantic field key
     * @param pointer display leaf pointer, or null for separately resolved content
     * @param format field representation
     */
    public LocalizableField(String key, String pointer, Format format) {
        this(key, pointer, format, null);
    }

    /**
     * Validates the field declaration. Pointer validation restricts the allowed root; the resource owner remains
     * responsible for choosing an appropriate display leaf.
     *
     * @throws NullPointerException if the key or format is null
     * @throws IllegalArgumentException if the key is blank, a pointer is outside spec or status, or a fallback is
     *     declared without a primary pointer
     */
    public LocalizableField {
        Objects.requireNonNull(key);
        Objects.requireNonNull(format);
        if (key.isBlank()) {
            throw new IllegalArgumentException("Field key must not be blank");
        }
        if (!isDisplayPointer(pointer) || !isDisplayPointer(fallbackPointer)) {
            throw new IllegalArgumentException(
                    "Fields must be semantic content or an explicitly declared spec or status display leaf");
        }
        if (pointer == null && fallbackPointer != null) {
            throw new IllegalArgumentException("A fallback requires a primary display pointer");
        }
    }

    /**
     * Selects the same effective display leaf for source extraction and projection.
     *
     * <p>With a fallback configured, nonblank textual primary values select the primary pointer; missing, null, blank
     * or non-textual primary values select the fallback. The fallback value itself is not validated here. This is a
     * source-field fallback, not a language fallback.
     *
     * @param source original resource as a JSON tree
     * @return selected pointer, or null for aggregated content
     */
    public String displayPointer(JsonNode source) {
        if (fallbackPointer == null) {
            return pointer;
        }
        var primary = source.at(pointer);
        if (primary.isTextual() && !primary.asText().isBlank()) {
            return pointer;
        }
        return fallbackPointer;
    }

    private static boolean isDisplayPointer(String pointer) {
        if (pointer == null) {
            return true;
        }
        if (pointer.startsWith("/spec/")) {
            return true;
        }
        return pointer.startsWith("/status/");
    }

    /**
     * Representation of a translatable field, independent of its storage location.
     *
     * @since 2.28.0
     */
    public enum Format {
        /**
         * Text without markup, such as a title, label or description. A descriptor may mark a plain-text field as
         * multiline without changing its format.
         */
        PLAIN_TEXT,

        /**
         * Rendered HTML intended for display. When paired with editable source content, declare both fields in a
         * {@link LocalizableDocument} so they are projected together.
         */
        HTML,

        /**
         * Editable document source, such as Markdown or an editor's raw HTML. The document binding's
         * {@link LocalizableDocument#rawType()} identifies its format; this value does not imply Markdown or
         * automatically render the source to HTML.
         */
        RAW_CONTENT
    }
}
