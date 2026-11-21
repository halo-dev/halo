package run.halo.app.localization;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import reactor.core.publisher.Mono;
import run.halo.app.extension.Extension;
import run.halo.app.extension.GroupVersionKind;

/**
 * Combines annotations with a resource owner's supplemental declaration.
 *
 * @since 2.28.0
 */
final class CombinedResourceLocalizationDescriptor implements ResourceLocalizationDescriptor {
    private final ResourceLocalizationDescriptor declarations;
    private final ResourceLocalizationDescriptor supplemental;
    private final List<LocalizableField> fields;
    private final List<LocalizableFieldCollection> fieldCollections;
    private final List<LocalizableDocument> documents;

    private CombinedResourceLocalizationDescriptor(
            ResourceLocalizationDescriptor declarations, ResourceLocalizationDescriptor supplemental) {
        this.declarations = declarations;
        this.supplemental = supplemental;
        fields = combine(declarations.fields(), supplemental.fields());
        fieldCollections = combine(declarations.fieldCollections(), supplemental.fieldCollections());
        documents = combine(declarations.documents(), supplemental.documents());
    }

    static Optional<ResourceLocalizationDescriptor> combine(
            ResourceLocalizationDescriptor declarations, ResourceLocalizationDescriptor supplemental) {
        if (!compatible(declarations, supplemental)) {
            return Optional.empty();
        }
        return Optional.of(new CombinedResourceLocalizationDescriptor(declarations, supplemental));
    }

    private static boolean compatible(
            ResourceLocalizationDescriptor declarations, ResourceLocalizationDescriptor supplemental) {
        if (!uniqueFields(combine(declarations.fields(), supplemental.fields()))) {
            return false;
        }
        if (!uniqueNamespaces(combine(declarations.fieldCollections(), supplemental.fieldCollections()))) {
            return false;
        }
        return uniqueDocuments(combine(declarations.documents(), supplemental.documents()));
    }

    private static boolean uniqueFields(List<LocalizableField> fields) {
        var keys = new HashSet<String>();
        if (fields.stream().map(LocalizableField::key).anyMatch(key -> !keys.add(key))) {
            return false;
        }
        for (int index = 0; index < fields.size(); index++) {
            for (int otherIndex = index + 1; otherIndex < fields.size(); otherIndex++) {
                if (pointersOverlap(
                        fields.get(index).pointer(), fields.get(otherIndex).pointer())) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean pointersOverlap(String left, String right) {
        if (left == null || right == null) {
            return false;
        }
        return left.equals(right) || left.startsWith(right + "/") || right.startsWith(left + "/");
    }

    private static boolean uniqueNamespaces(List<LocalizableFieldCollection> collections) {
        var namespaces = new HashSet<String>();
        return collections.stream().map(LocalizableFieldCollection::namespace).allMatch(namespaces::add);
    }

    private static boolean uniqueDocuments(List<LocalizableDocument> documents) {
        var keys = new HashSet<String>();
        for (var document : documents) {
            if (!keys.add(document.htmlField())) {
                return false;
            }
            if (document.rawField() != null && !keys.add(document.rawField())) {
                return false;
            }
        }
        return true;
    }

    private static <T> List<T> combine(List<T> declarations, List<T> supplemental) {
        return Stream.concat(declarations.stream(), supplemental.stream()).toList();
    }

    @Override
    public GroupVersionKind supports() {
        return supplemental.supports();
    }

    @Override
    public List<LocalizableField> fields() {
        return fields;
    }

    @Override
    public List<LocalizableFieldCollection> fieldCollections() {
        return fieldCollections;
    }

    @Override
    public List<LocalizableField> resolveDisplayFields(JsonNode source) {
        var resolved = new ArrayList<LocalizableField>();
        resolved.addAll(declarations.resolveDisplayFields(source));
        resolved.addAll(supplemental.resolveDisplayFields(source));
        return LocalizableFieldResolver.resolveCombined(resolved);
    }

    @Override
    public List<LocalizableDocument> documents() {
        return documents;
    }

    @Override
    public Mono<List<LocalizableDocument>> resolveDocuments(Extension resource) {
        return supplemental.resolveDocuments(resource);
    }

    @Override
    public Mono<Map<String, String>> resolveFields(Extension resource, Map<String, String> extracted) {
        return supplemental.resolveFields(resource, extracted);
    }

    @Override
    public String displayName() {
        var displayName = supplemental.displayName();
        if (Objects.equals(displayName, supplemental.supports().kind())) {
            return declarations.displayName();
        }
        return displayName;
    }

    @Override
    public String titleField() {
        var title = supplemental.titleField();
        return title == null ? declarations.titleField() : title;
    }

    @Override
    public String summaryField() {
        var summary = supplemental.summaryField();
        return summary == null ? declarations.summaryField() : summary;
    }

    @Override
    public boolean isEligible(Extension resource) {
        return supplemental.isEligible(resource);
    }
}
