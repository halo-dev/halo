package run.halo.app.localization;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import run.halo.app.content.ContentWrapper;
import run.halo.app.extension.Extension;

/** Adapts Halo's reconstructed content wrapper to the two semantic fields used by the built-in content descriptors. */
final class ContentLocalizationDocuments {
    static final String HTML_FIELD = "content.html";
    static final String RAW_FIELD = "content.raw";
    static final String PREFERRED_EDITOR = "content.halo.run/preferred-editor";

    private ContentLocalizationDocuments() {}

    static Map<String, String> fields(Extension resource, Map<String, String> extracted, ContentWrapper content) {
        var values = new LinkedHashMap<>(extracted);
        var document = document(resource, content);
        if (document == null) {
            values.remove(HTML_FIELD);
            values.remove(RAW_FIELD);
            return values;
        }
        values.put(HTML_FIELD, Objects.toString(content.getContent(), ""));
        values.put(RAW_FIELD, Objects.toString(content.getRaw(), ""));
        return values;
    }

    static List<LocalizableDocument> documents(Extension resource, ContentWrapper content) {
        var document = document(resource, content);
        return document == null ? List.of() : List.of(document);
    }

    private static LocalizableDocument document(Extension resource, ContentWrapper content) {
        if (content == null
                || content.getRawType() == null
                || content.getRawType().isBlank()) {
            return null;
        }
        var annotations =
                resource.getMetadata() == null ? null : resource.getMetadata().getAnnotations();
        var editor = annotations == null ? null : annotations.get(PREFERRED_EDITOR);
        if (editor != null && editor.isBlank()) {
            editor = null;
        }
        try {
            return new LocalizableDocument(HTML_FIELD, RAW_FIELD, content.getRawType(), editor);
        } catch (IllegalArgumentException error) {
            // A malformed storage format cannot be offered to the editor; metadata fields remain usable.
            return null;
        }
    }
}
