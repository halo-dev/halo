package run.halo.app.localization;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Shared source-specific binding resolution for Extension and domain descriptors. */
final class LocalizableFieldResolver {
    private LocalizableFieldResolver() {}

    static List<LocalizableField> resolve(
            List<LocalizableField> fields, List<LocalizableFieldCollection> collections, JsonNode source) {
        var result = new ArrayList<>(fields);
        var keys = new HashSet<String>();
        var pointers = new HashSet<String>();
        result.forEach(field -> {
            keys.add(field.key());
            pointers.add(field.pointer());
        });
        var namespaces = collections.stream()
                .collect(Collectors.groupingBy(LocalizableFieldCollection::namespace, Collectors.counting()));
        var dynamic = collections.stream()
                .filter(collection -> namespaces.get(collection.namespace()) == 1)
                .flatMap(collection -> collection.resolve(source).stream())
                .toList();
        var keyCounts = dynamic.stream()
                .map(LocalizableField::key)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        var pointerCounts = dynamic.stream()
                .map(LocalizableField::pointer)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        for (var field : dynamic) {
            if (keys.contains(field.key()) || pointers.contains(field.pointer())) {
                continue;
            }
            if (keyCounts.get(field.key()) == 1 && pointerCounts.get(field.pointer()) == 1) {
                result.add(field);
            }
        }
        return result.stream()
                .filter(field -> independentPointer(field, result))
                .toList();
    }

    /** Combines already resolved declaration sets while removing ambiguous keys and display pointers. */
    static List<LocalizableField> resolveCombined(List<LocalizableField> fields) {
        var keyCounts = fields.stream()
                .map(LocalizableField::key)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        var pointerCounts = fields.stream()
                .map(LocalizableField::pointer)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        return fields.stream()
                .filter(field -> keyCounts.get(field.key()) == 1)
                .filter(field -> field.pointer() == null || pointerCounts.get(field.pointer()) == 1)
                .filter(field -> independentPointer(field, fields))
                .toList();
    }

    /**
     * Filters public links that cannot be projected safely against the current display fields.
     *
     * <p>A link pointer must be a non-empty absolute JSON pointer, appear once, and not overlap a declared field
     * pointer. Invalid links are ignored independently so malformed descriptor additions cannot overwrite translated
     * values or prevent unrelated fields from being projected.
     */
    static List<String> validLinkPointers(List<LocalizableField> fields, List<String> links) {
        if (links == null || links.isEmpty()) {
            return List.of();
        }
        var declaredFields = fields == null ? List.<LocalizableField>of() : fields;
        var seen = new HashSet<String>();
        var valid = new ArrayList<String>();
        for (var link : links) {
            if (!validLinkPointer(link, declaredFields, seen)) {
                continue;
            }
            seen.add(link);
            valid.add(link);
        }
        return List.copyOf(valid);
    }

    private static boolean validLinkPointer(String link, List<LocalizableField> fields, Set<String> seen) {
        if (link == null || link.isEmpty() || !link.startsWith("/") || seen.contains(link)) {
            return false;
        }
        try {
            JsonPointer.compile(link);
        } catch (IllegalArgumentException error) {
            return false;
        }
        for (var field : fields) {
            if (field == null || field.pointer() == null) {
                continue;
            }
            try {
                if (pointersOverlap(link, field.pointer())) {
                    return false;
                }
            } catch (IllegalArgumentException error) {
                return false;
            }
        }
        return true;
    }

    private static boolean independentPointer(LocalizableField field, List<LocalizableField> fields) {
        var pointer = field.pointer();
        if (pointer == null) {
            return true;
        }
        return fields.stream()
                .filter(other -> other != field)
                .map(LocalizableField::pointer)
                .filter(java.util.Objects::nonNull)
                .noneMatch(other -> pointersOverlap(pointer, other));
    }

    private static boolean pointersOverlap(String left, String right) {
        var first = JsonPointer.compile(left);
        var second = JsonPointer.compile(right);
        while (!first.matches() && !second.matches()) {
            if (!first.getMatchingProperty().equals(second.getMatchingProperty())) {
                return false;
            }
            first = first.tail();
            second = second.tail();
        }
        return true;
    }
}
