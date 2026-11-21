package run.halo.app.localization;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import reactor.core.publisher.Mono;
import run.halo.app.extension.Extension;
import run.halo.app.extension.GroupVersionKind;

/** Locale-specific view over declaration metadata; source values and translation identities are untouched. */
final class LocalizedResourceLocalizationDescriptor implements ResourceLocalizationDescriptor {
    private final ResourceLocalizationDescriptor delegate;
    private final DescriptorLabels labels;

    LocalizedResourceLocalizationDescriptor(ResourceLocalizationDescriptor delegate, UnaryOperator<String> labels) {
        this.delegate = delegate;
        this.labels = new DescriptorLabels(labels);
    }

    @Override
    public GroupVersionKind supports() {
        return delegate.supports();
    }

    @Override
    public List<LocalizableField> fields() {
        return delegate.fields().stream().map(labels::field).toList();
    }

    @Override
    public List<LocalizableFieldCollection> fieldCollections() {
        return delegate.fieldCollections().stream().map(labels::collection).toList();
    }

    @Override
    public List<LocalizableField> resolveDisplayFields(JsonNode source) {
        return delegate.resolveDisplayFields(source).stream().map(labels::field).toList();
    }

    @Override
    public List<LocalizableDocument> documents() {
        return delegate.documents();
    }

    @Override
    public Mono<List<LocalizableDocument>> resolveDocuments(Extension resource) {
        return delegate.resolveDocuments(resource);
    }

    @Override
    public Mono<Map<String, String>> resolveFields(Extension resource, Map<String, String> extracted) {
        return delegate.resolveFields(resource, extracted);
    }

    @Override
    public String displayName() {
        return labels.resolve(delegate.displayName());
    }

    @Override
    public String titleField() {
        return delegate.titleField();
    }

    @Override
    public String summaryField() {
        return delegate.summaryField();
    }

    @Override
    public boolean isEligible(Extension resource) {
        return delegate.isEligible(resource);
    }
}

/** Locale-specific view over a non-Extension source declaration. */
final class LocalizedLocalizableResourceDescriptor<T> implements LocalizableResourceDescriptor<T> {
    private final LocalizableResourceDescriptor<T> delegate;
    private final DescriptorLabels labels;

    LocalizedLocalizableResourceDescriptor(LocalizableResourceDescriptor<T> delegate, UnaryOperator<String> labels) {
        this.delegate = delegate;
        this.labels = new DescriptorLabels(labels);
    }

    @Override
    public String namespace() {
        return delegate.namespace();
    }

    @Override
    public String type() {
        return delegate.type();
    }

    @Override
    public Class<T> resourceClass() {
        return delegate.resourceClass();
    }

    @Override
    public com.fasterxml.jackson.databind.node.ObjectNode toJson(T source) {
        return delegate.toJson(source);
    }

    @Override
    public T fromJson(com.fasterxml.jackson.databind.node.ObjectNode source) {
        return delegate.fromJson(source);
    }

    @Override
    public ResourceReference reference(T source) {
        return delegate.reference(source);
    }

    @Override
    public List<LocalizableField> fields() {
        return delegate.fields().stream().map(labels::field).toList();
    }

    @Override
    public List<LocalizableFieldCollection> fieldCollections() {
        return delegate.fieldCollections().stream().map(labels::collection).toList();
    }

    @Override
    public List<LocalizableField> resolveDisplayFields(JsonNode source) {
        return delegate.resolveDisplayFields(source).stream().map(labels::field).toList();
    }

    @Override
    public List<LocalizableDocument> documents() {
        return delegate.documents();
    }

    @Override
    public Mono<List<LocalizableDocument>> resolveDocuments(T source) {
        return delegate.resolveDocuments(source);
    }

    @Override
    public String displayName() {
        return labels.resolve(delegate.displayName());
    }

    @Override
    public String titleField() {
        return delegate.titleField();
    }

    @Override
    public String summaryField() {
        return delegate.summaryField();
    }

    @Override
    public boolean isEligible(T source) {
        return delegate.isEligible(source);
    }
}

/** Applies message resolution to descriptor metadata without changing semantic declarations. */
final class DescriptorLabels {
    private final UnaryOperator<String> resolver;

    DescriptorLabels(UnaryOperator<String> resolver) {
        this.resolver = resolver;
    }

    String resolve(String value) {
        return resolver.apply(value);
    }

    LocalizableField field(LocalizableField field) {
        return new LocalizableField(
                field.key(), field.pointer(), field.format(), schema(field.schema()), resolve(field.group()));
    }

    LocalizableFieldCollection collection(LocalizableFieldCollection collection) {
        var definitions = collection.definitions().stream()
                .map(definition -> new LocalizableFieldCollection.EntryDefinition(
                        definition.identityField(), definition.identityValue(), schema(definition.schema())))
                .toList();
        return new LocalizableFieldCollection(
                collection.namespace(),
                collection.pointer(),
                collection.identityFields(),
                collection.valueField(),
                collection.fallbackFormat(),
                definitions,
                resolve(collection.group()));
    }

    LocalizableSchema schema(LocalizableSchema schema) {
        var properties = new LinkedHashMap<String, LocalizableSchema>();
        schema.properties().forEach((name, value) -> properties.put(name, schema(value)));
        return new LocalizableSchema(
                schema.type(),
                schema.policy(),
                schema.format(),
                resolve(schema.label()),
                schema.required(),
                schema.multiline(),
                schema.nullable(),
                properties,
                schema.items() == null ? null : schema(schema.items()),
                schema.identityField(),
                schema.independentStructure());
    }
}
