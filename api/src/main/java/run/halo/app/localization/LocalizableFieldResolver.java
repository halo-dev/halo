package run.halo.app.localization;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
        var first = com.fasterxml.jackson.core.JsonPointer.compile(left);
        var second = com.fasterxml.jackson.core.JsonPointer.compile(right);
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
