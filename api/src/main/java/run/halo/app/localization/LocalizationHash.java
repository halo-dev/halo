package run.halo.app.localization;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Source-content fingerprints shared by translation producers and projection consumers.
 *
 * <p>Capture a fingerprint when translating and retain it with the translated value. Core compares it with the current
 * source before displaying a translation. This detects changed source content; it is not a resource identifier, an
 * authentication mechanism or a translation-quality check.
 *
 * <p>Use {@link #of} for an independent field and {@link #sourceFields} for documents. A document's rendered HTML and
 * editable source must share the combined fingerprint, rather than separate hashes.
 *
 * @since 2.28.0
 */
public final class LocalizationHash {
    private LocalizationHash() {}

    /**
     * Computes an exact SHA-256 fingerprint of a source value encoded as UTF-8.
     *
     * <p>No whitespace, markup or Unicode normalization is performed, so even formatting changes produce a different
     * fingerprint.
     *
     * @param value source field value
     * @return the 64-character lowercase hexadecimal fingerprint used to reject stale translations
     * @throws NullPointerException if the source value is null
     */
    public static String of(String value) {
        try {
            return HexFormat.of()
                    .formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Fingerprints resolved source values, treating each document as a unit.
     *
     * <p>Each document hash includes its case-normalized source format, semantic field keys, rendered HTML and optional
     * editable source. Both parts receive the same hash. If a required key is absent, none of that document's parts
     * receives a hash. Fields outside the supplied document bindings are hashed individually. Present null values are
     * treated as empty strings.
     *
     * <p>For example, both entries below receive a shared fingerprint:
     *
     * <pre>{@code
     * var document = new LocalizableDocument("body", "raw", "markdown", null);
     * var source = Map.of("body", "<p>Hello</p>", "raw", "Hello");
     * var hashes = LocalizationHash.sourceFields(source, List.of(document));
     * var bodyHash = hashes.get("body");
     * var rawHash = hashes.get("raw");
     * }</pre>
     *
     * @param fields original values indexed by semantic field key, before localization
     * @param documents document bindings resolved for the source resource's current formats
     * @return fingerprints indexed by field key; incomplete document bindings have no entries
     */
    public static Map<String, String> sourceFields(Map<String, String> fields, List<LocalizableDocument> documents) {
        var hashes = new LinkedHashMap<String, String>();
        var documentFields = new HashSet<String>();
        for (var document : documents) {
            var html = document.htmlField();
            var raw = document.rawField();
            documentFields.add(html);
            if (raw != null) {
                documentFields.add(raw);
            }
            if (!fields.containsKey(html) || (raw != null && !fields.containsKey(raw))) {
                continue;
            }
            var content =
                    framed(document.rawType().toLowerCase(Locale.ROOT)) + framed(html) + framed(value(fields, html));
            if (raw != null) {
                content += framed(raw) + framed(value(fields, raw));
            }
            var hash = of("document:" + content);
            hashes.put(html, hash);
            if (raw != null) {
                hashes.put(raw, hash);
            }
        }
        fields.forEach((key, ignored) -> {
            if (!documentFields.contains(key)) {
                hashes.put(key, of(value(fields, key)));
            }
        });
        return hashes;
    }

    private static String value(Map<String, String> fields, String key) {
        return Objects.toString(fields.get(key), "");
    }

    private static String framed(String value) {
        return value.length() + ":" + value;
    }
}
