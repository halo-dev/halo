package run.halo.app.localization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.JsonNodeFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/** Annotation-based descriptor factory for detached non-Extension display DTOs. */
public final class LocalizableResourceDescriptors {
    private static final ObjectMapper DEFAULT_MAPPER = JsonMapper.builder()
            .findAndAddModules()
            .configure(JsonNodeFeature.STRIP_TRAILING_BIGDECIMAL_ZEROES, false)
            .build();

    private LocalizableResourceDescriptors() {}

    /**
     * Compiles one registered DTO type using Jackson's property model. The class requires {@link LocalizableResource}
     * with a namespace and type, and exactly one {@link LocalizationId} scalar property. No classpath scan occurs.
     *
     * @param resourceClass registered detached DTO type
     * @param mapper the same serialization mapper used by its public API
     * @param <T> display DTO type
     * @return immutable declaration shared by the source adapter and presentation client
     * @since 2.28.0
     */
    public static <T> LocalizableResourceDescriptor<T> fromAnnotations(Class<T> resourceClass, ObjectMapper mapper) {
        if (run.halo.app.extension.Extension.class.isAssignableFrom(resourceClass)) {
            throw new IllegalArgumentException("Extension types use their registered Extension descriptor");
        }
        var annotation = Objects.requireNonNull(
                resourceClass.getAnnotation(LocalizableResource.class),
                "A domain resource requires @LocalizableResource");
        ResourceReference.external(annotation.namespace(), annotation.type(), "validation");
        var codec = Objects.requireNonNull(mapper)
                .copy()
                .configure(JsonNodeFeature.STRIP_TRAILING_BIGDECIMAL_ZEROES, false);
        var compiler = new LocalizableAnnotations(codec);
        var identity = Objects.requireNonNull(
                compiler.identityPointer(resourceClass), "A domain resource requires @LocalizationId");
        var fields = compiler.fields(resourceClass);
        var linkPointers = compiler.linkPointers(resourceClass);
        var catalogTitle = compiler.titleField(resourceClass);
        var catalogSummary = compiler.summaryField(resourceClass);
        validateDeclaration(fields, linkPointers, identity);
        return new LocalizableResourceDescriptor<>() {
            @Override
            public String namespace() {
                return annotation.namespace();
            }

            @Override
            public String type() {
                return annotation.type();
            }

            @Override
            public Class<T> resourceClass() {
                return resourceClass;
            }

            @Override
            public ObjectNode toJson(T source) {
                return objectTree(codec, source);
            }

            @Override
            public T fromJson(ObjectNode source) {
                return codec.convertValue(source, resourceClass);
            }

            @Override
            public ResourceReference reference(T source) {
                var value = toJson(source).at(identity);
                if (!value.isTextual() && !value.isIntegralNumber()) {
                    throw new IllegalArgumentException("A resource identity must be text or an integer");
                }
                return ResourceReference.external(namespace(), type(), value.asText());
            }

            @Override
            public List<LocalizableField> fields() {
                return fields;
            }

            @Override
            public List<String> linkPointers() {
                return linkPointers;
            }

            @Override
            public String displayName() {
                return annotation.label().isBlank() ? type() : annotation.label();
            }

            @Override
            public String titleField() {
                return catalogTitle;
            }

            @Override
            public String summaryField() {
                return catalogSummary;
            }
        };
    }

    static ObjectNode toJson(Object source) {
        return objectTree(DEFAULT_MAPPER, source);
    }

    static <T> T fromJson(ObjectNode source, Class<T> resourceClass) {
        return DEFAULT_MAPPER.convertValue(source, resourceClass);
    }

    private static ObjectNode objectTree(ObjectMapper mapper, Object source) {
        var tree = mapper.valueToTree(source);
        if (!(tree instanceof ObjectNode object)) {
            throw new IllegalArgumentException("A localizable resource must serialize as a JSON object");
        }
        return object;
    }

    private static void validateDeclaration(List<LocalizableField> fields, List<String> linkPointers, String identity) {
        validateFields(fields, identity);
        validateLinks(linkPointers, fields, identity);
    }

    private static void validateFields(List<LocalizableField> fields, String identity) {
        for (var field : fields) {
            var pointer = field.pointer();
            if (pointer != null && overlaps(identity, pointer)) {
                throw new IllegalArgumentException("A resource identity cannot overlap localized fields");
            }
        }
    }

    private static void validateLinks(List<String> linkPointers, List<LocalizableField> fields, String identity) {
        var links = new HashSet<String>();
        for (var pointer : linkPointers) {
            if (!validLinkPointer(pointer) || !links.add(pointer)) {
                throw new IllegalArgumentException("Public links require unique absolute JSON pointers");
            }
            if (overlaps(identity, pointer)) {
                throw new IllegalArgumentException("A resource identity cannot overlap public links");
            }
            for (var field : fields) {
                var fieldPointer = field.pointer();
                if (fieldPointer != null && overlaps(fieldPointer, pointer)) {
                    throw new IllegalArgumentException("Translated fields cannot overlap public links");
                }
            }
        }
    }

    private static boolean validLinkPointer(String pointer) {
        return pointer != null && !pointer.isEmpty() && pointer.startsWith("/");
    }

    private static boolean overlaps(String left, String right) {
        return left.equals(right) || left.startsWith(right + "/") || right.startsWith(left + "/");
    }
}
