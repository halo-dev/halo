package run.halo.app.localization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.extension.Extension;
import run.halo.app.infra.utils.JsonUtils;

/** Textual metadata only; tag identity, URLs and indexing directives remain source-owned. */
final class ContentSeoFields {
    private static final List<String> NAMES =
            List.of("description", "keywords", "og:title", "og:description", "twitter:title", "twitter:description");

    private ContentSeoFields() {}

    static List<LocalizableField> fields() {
        return NAMES.stream()
                .map(name -> new LocalizableField("seo." + name, null, LocalizableField.Format.PLAIN_TEXT))
                .toList();
    }

    static Map<String, String> extract(Extension source) {
        if (!supports(source)) {
            return Map.of();
        }
        JsonNode tree = JsonUtils.mapper().valueToTree(source);
        return extract(tree.path("spec").path("htmlMetas"));
    }

    private static Map<String, String> extract(JsonNode metas) {
        var values = new LinkedHashMap<String, String>();
        var duplicates = new java.util.HashSet<String>();
        for (var meta : metas) {
            String key = key(meta);
            if (key == null || !meta.path("content").isTextual()) {
                continue;
            }
            if (values.putIfAbsent(key, meta.path("content").asText()) != null) {
                duplicates.add(key);
            }
        }
        duplicates.forEach(values::remove);
        return values;
    }

    static boolean apply(Extension source, ObjectNode tree, Map<String, String> translations) {
        if (!supports(source)) {
            return false;
        }
        var metas = tree.path("spec").path("htmlMetas");
        var allowed = extract(metas).keySet();
        boolean changed = false;
        for (var meta : metas) {
            String key = key(meta);
            if (!allowed.contains(key) || !translations.containsKey(key)) {
                continue;
            }
            String value = translations.get(key);
            if (value.equals(meta.path("content").asText())) {
                continue;
            }
            ((ObjectNode) meta).put("content", value);
            changed = true;
        }
        return changed;
    }

    private static String key(JsonNode meta) {
        String name = meta.path("name").asText("").toLowerCase(Locale.ROOT);
        String property = meta.path("property").asText("").toLowerCase(Locale.ROOT);
        if (!name.isEmpty() && !property.isEmpty()) {
            return null;
        }
        String identity = property.isEmpty() ? name : property;
        return NAMES.contains(identity) ? "seo." + identity : null;
    }

    private static boolean supports(Extension source) {
        return source instanceof Post || source instanceof SinglePage;
    }
}
