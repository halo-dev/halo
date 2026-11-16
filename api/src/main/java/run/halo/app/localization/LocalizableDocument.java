package run.halo.app.localization;

import java.util.Objects;

/**
 * Binds the rendered HTML and optional editable source of one translatable document.
 *
 * <p>Field keys refer to entries in {@link ResourceLocalizationDescriptor#fields()}, never JSON pointers. The HTML
 * field uses {@link LocalizableField.Format#HTML}; a separate source field uses
 * {@link LocalizableField.Format#RAW_CONTENT}. A resource may declare several independent documents.
 *
 * <p>Core projects a document's fields together from one language only when all parts have an approved translation and
 * match the source fingerprint. This prevents a translated HTML value from being combined with unrelated or outdated
 * editable source. Use {@link LocalizationHash#sourceFields} when creating the fingerprints.
 *
 * <p>For example, these bindings describe HTML-only content and a document with Markdown source:
 *
 * <pre>{@code
 * var announcement = new LocalizableDocument("announcement", null, "html", null);
 * var body = new LocalizableDocument("body", "raw", "markdown", null);
 * }</pre>
 *
 * <p>The binding describes the content; it does not render Markdown or install an editor. A descriptor can resolve the
 * format and editor per resource through {@link ResourceLocalizationDescriptor#resolveDocuments} while retaining the
 * declared field keys.
 *
 * @param htmlField nonblank semantic key for rendered HTML
 * @param rawField distinct, nonblank semantic key for editable source, or null for HTML-only content
 * @param rawType nonblank source format understood by the editor, such as {@code markdown} or {@code html}; limited to
 *     100 characters and must be HTML, ignoring case, when rawField is null
 * @param editor required editor provider name, or null to select a compatible editor; this is an editor identifier, not
 *     its display label
 * @since 2.28.0
 */
public record LocalizableDocument(String htmlField, String rawField, String rawType, String editor) {
    /**
     * Validates the binding's keys and source format. Membership in the descriptor's field allowlist is checked
     * separately when resolving translations.
     *
     * @throws NullPointerException if the HTML key or source format is null
     * @throws IllegalArgumentException if a key or format is blank, the format exceeds 100 characters, the field keys
     *     are equal, or a non-HTML document has no raw field
     */
    public LocalizableDocument {
        Objects.requireNonNull(htmlField);
        Objects.requireNonNull(rawType);
        if (htmlField.isBlank()) {
            throw new IllegalArgumentException("HTML field must not be blank");
        }
        if (rawType.isBlank()) {
            throw new IllegalArgumentException("Raw type must not be blank");
        }
        if (rawType.length() > 100) {
            throw new IllegalArgumentException("Raw type must not exceed 100 characters");
        }
        if (rawField != null) {
            if (rawField.isBlank()) {
                throw new IllegalArgumentException("Raw field must not be blank");
            }
            if (rawField.equals(htmlField)) {
                throw new IllegalArgumentException("Raw and HTML fields must be distinct");
            }
        } else if (!rawType.equalsIgnoreCase("HTML")) {
            throw new IllegalArgumentException("Non-HTML documents require a raw field");
        }
    }
}
