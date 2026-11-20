package run.halo.app.localization;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Shared patch validation for Extension resources and detached domain display DTOs. */
final class LocalizationProjection {
    private LocalizationProjection() {}

    static Map<String, String> translations(
            ResourceReference subject,
            Map<String, String> sources,
            List<LocalizableDocument> declaredDocuments,
            List<LocalizableField> displayFields,
            List<LocalizableDocument> documents,
            List<LocalizationPatch> patches,
            LocalizationContext context) {
        var result = new LinkedHashMap<String, String>();
        var locales = new ArrayList<Locale>();
        locales.add(context.targetLocale());
        locales.addAll(context.fallbackLocales());
        var candidates = locales.stream()
                .distinct()
                .takeWhile(locale -> !locale.equals(context.sourceLocale()))
                .map(locale -> findPatch(subject, locale, patches))
                .flatMap(Optional::stream)
                .toList();
        var schemas = new LinkedHashMap<String, LocalizableSchema>();
        displayFields.forEach(field -> schemas.put(field.key(), field.schema()));
        var documentFields = new HashSet<String>();
        var declared = new LinkedHashMap<String, List<String>>();
        for (var document : declaredDocuments) {
            var keys = documentKeys(document);
            declared.put(document.htmlField(), keys);
            documentFields.addAll(keys);
        }
        var resolved = matchingDocuments(documents, declared);
        var sourceHashes = LocalizationHash.sourceFields(sources, resolved);
        for (var document : resolved) {
            var keys = documentKeys(document);
            if (!schemas.keySet().containsAll(keys)) {
                continue;
            }
            firstDocumentTranslation(candidates, keys, sourceHashes, sources, schemas)
                    .forEach(field -> result.put(field.key(), translatedValue(field, sources, schemas)));
        }
        for (var key : sourceHashes.keySet()) {
            if (!schemas.containsKey(key) || documentFields.contains(key)) {
                continue;
            }
            firstFieldTranslation(candidates, key, sourceHashes, sources, schemas)
                    .ifPresent(field -> result.put(key, translatedValue(field, sources, schemas)));
        }
        return result;
    }

    private static List<LocalizationPatch.Field> firstDocumentTranslation(
            List<LocalizationPatch> candidates,
            List<String> keys,
            Map<String, String> sourceHashes,
            Map<String, String> sources,
            Map<String, LocalizableSchema> schemas) {
        return candidates.stream()
                .map(patch -> patch.fields().stream()
                        .filter(field -> keys.contains(field.key()))
                        .toList())
                .filter(fields -> fields.size() == keys.size())
                .filter(fields -> fields.stream()
                                .map(LocalizationPatch.Field::key)
                                .distinct()
                                .count()
                        == keys.size())
                .filter(fields -> fields.stream()
                        .allMatch(field -> canApply(field, sourceHashes) && validValue(field, sources, schemas)))
                .findFirst()
                .orElseGet(List::of);
    }

    private static Optional<LocalizationPatch.Field> firstFieldTranslation(
            List<LocalizationPatch> candidates,
            String key,
            Map<String, String> sourceHashes,
            Map<String, String> sources,
            Map<String, LocalizableSchema> schemas) {
        return candidates.stream()
                .map(patch -> patch.fields().stream()
                        .filter(field -> Objects.equals(field.key(), key))
                        .toList())
                .filter(fields -> fields.size() == 1)
                .map(List::getFirst)
                .filter(field -> canApply(field, sourceHashes) && validValue(field, sources, schemas))
                .findFirst();
    }

    private static boolean validValue(
            LocalizationPatch.Field field, Map<String, String> sources, Map<String, LocalizableSchema> schemas) {
        var schema = schemas.get(field.key());
        if (schema == null || schema.policy() == LocalizableSchema.Policy.PRESERVE) {
            return false;
        }
        try {
            LocalizableValues.validate(schema, Objects.toString(sources.get(field.key()), ""), field.value());
            return true;
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    private static String translatedValue(
            LocalizationPatch.Field field, Map<String, String> sources, Map<String, LocalizableSchema> schemas) {
        var schema = schemas.get(field.key());
        if (!schema.structured()) {
            return field.value();
        }
        var source = LocalizableValues.decode(schema, sources.get(field.key()));
        var candidate = LocalizableValues.decode(schema, field.value());
        return LocalizableValues.encode(schema, LocalizableValues.project(schema, source, candidate));
    }

    private static List<LocalizableDocument> matchingDocuments(
            List<LocalizableDocument> documents, Map<String, List<String>> declared) {
        var resolved = new ArrayList<LocalizableDocument>();
        var seen = new HashSet<String>();
        for (var document : documents) {
            var key = document.htmlField();
            if (!documentKeys(document).equals(declared.get(key)) || !seen.add(key)) {
                throw new IllegalArgumentException("Resolved document bindings differ from declaration");
            }
            resolved.add(document);
        }
        return resolved;
    }

    private static List<String> documentKeys(LocalizableDocument document) {
        if (document.rawField() == null) {
            return List.of(document.htmlField());
        }
        return List.of(document.htmlField(), document.rawField());
    }

    private static Optional<LocalizationPatch> findPatch(
            ResourceReference subject, Locale locale, List<LocalizationPatch> patches) {
        var matching = patches.stream()
                .filter(patch -> subject.equals(patch.subject()))
                .filter(patch -> locale.equals(patch.locale()))
                .toList();
        // Ambiguous responses must not depend on provider emission order.
        if (matching.size() != 1) {
            return Optional.empty();
        }
        return Optional.of(matching.getFirst());
    }

    private static boolean canApply(LocalizationPatch.Field field, Map<String, String> sourceHashes) {
        if (!sourceHashes.containsKey(field.key())) {
            return false;
        }
        if (field.value() == null) {
            return false;
        }
        if (!sourceHashes.get(field.key()).equals(field.sourceHash())) {
            return false;
        }
        return field.state() == LocalizationPatch.State.APPROVED;
    }
}
