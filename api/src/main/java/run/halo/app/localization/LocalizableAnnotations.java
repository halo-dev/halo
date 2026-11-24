package run.halo.app.localization;

import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import run.halo.app.extension.Extension;
import run.halo.app.extension.GroupVersionKind;

/**
 * Compiles explicitly registered classes into the same schemas used by programmatic descriptors. Jackson's actual
 * serialized property names determine pointers; annotations determine semantics. Ignored, write-only, unwrapped,
 * custom-serialized and ambiguous declarations are rejected rather than guessed. Dynamic keys and separately stored
 * documents remain programmatic declarations.
 *
 * <p>One compiler can be reused with the application's ObjectMapper. ClassValue entries follow the class lifecycle, so
 * unloading a plugin does not retain its classloader through a global class-keyed map.
 *
 * <pre>{@code
 * var annotations = new LocalizableAnnotations(mapper);
 * var fields = annotations.fields(Notice.class);
 * var descriptor = annotations.descriptor(Notice.class);
 * }</pre>
 *
 * @since 2.28.0
 */
public final class LocalizableAnnotations {
    private static final String POINTER_SEPARATOR = "/";

    private static final String POINTER_TILDE_ESCAPE = "~0";

    private static final String POINTER_SLASH_ESCAPE = "~1";

    private final ObjectMapper mapper;
    private final ClassValue<Declaration> declarations = new ClassValue<>() {
        @Override
        protected Declaration computeValue(Class<?> type) {
            if (!hasDeclarations(type)) {
                return new Declaration(List.of(), List.of(), List.of(), null, null);
            }
            var state = new CollectionState(
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new HashSet<>());
            collect(mapper.constructType(type), "", state);
            validateFields(state.fields());
            validateLinkPointers(state.fields(), state.linkPointers());
            if (state.identities().size() > 1) {
                throw invalid("A resource must declare at most one localization identity");
            }
            return new Declaration(
                    List.copyOf(state.fields()),
                    List.copyOf(state.identities()),
                    List.copyOf(state.linkPointers()),
                    catalogField(state.titleFields(), "title"),
                    catalogField(state.summaryFields(), "summary"));
        }
    };

    /** Creates a compiler using the same mapping rules as the public resource representation. */
    public LocalizableAnnotations(ObjectMapper mapper) {
        this.mapper = Objects.requireNonNull(mapper);
    }

    /** Returns immutable declarations; explicit semantic keys must be unique across all traversed objects. */
    public List<LocalizableField> fields(Class<?> type) {
        return declarations.get(type).fields();
    }

    /** Returns the one declared stable identity pointer, rejecting missing or ambiguous identity declarations. */
    public String identityPointer(Class<?> type) {
        var identities = declarations.get(type).identities();
        if (identities.isEmpty()) {
            throw invalid("A non-Extension resource requires @LocalizationId");
        }
        return identities.getFirst();
    }

    /** Returns the declared semantic key used as the resource title, or null when omitted. */
    public String titleField(Class<?> type) {
        return declarations.get(type).titleField();
    }

    /** Returns the declared semantic key used as the resource summary, or null when omitted. */
    public String summaryField(Class<?> type) {
        return declarations.get(type).summaryField();
    }

    /** Returns the declared source-owned public URL pointers. */
    public List<String> linkPointers(Class<?> type) {
        return declarations.get(type).linkPointers();
    }

    /**
     * Creates an Extension descriptor from annotations. The caller registers the type through the normal SchemeManager.
     */
    public ResourceLocalizationDescriptor descriptor(Class<? extends Extension> type) {
        var declaration = declarations.get(type);
        var fields = declaration.fields();
        var metadata = type.getAnnotation(LocalizableResource.class);
        var gvk = GroupVersionKind.fromExtension(type);
        var label = metadata == null || metadata.label().isBlank() ? gvk.kind() : metadata.label();
        return new ResourceLocalizationDescriptor() {
            @Override
            public GroupVersionKind supports() {
                return gvk;
            }

            @Override
            public List<LocalizableField> fields() {
                return fields;
            }

            @Override
            public List<String> linkPointers() {
                return declaration.linkPointers();
            }

            @Override
            public String displayName() {
                return label;
            }

            @Override
            public String titleField() {
                return declaration.titleField();
            }

            @Override
            public String summaryField() {
                return declaration.summaryField();
            }
        };
    }

    private void collect(JavaType type, String parent, CollectionState state) {
        enter(type, state.ancestors());
        try {
            for (var property : properties(type)) {
                collect(property, parent, state);
            }
        } finally {
            state.ancestors().remove(type.getRawClass());
        }
    }

    private void collect(Property property, String parent, CollectionState state) {
        var path = parent + POINTER_SEPARATOR + escape(property.definition().getName());
        if (property.identity()) {
            validateIdentity(property);
            state.identities().add(path);
        }
        if (property.link()) {
            validateLink(property);
            state.linkPointers().add(path);
        }
        if (property.nested()) {
            collect(property.type(), path, state);
        }
        if (property.annotation() != null) {
            collectLocalizedField(property, path, state);
        }
    }

    private void collectLocalizedField(Property property, String path, CollectionState state) {
        var annotation = property.annotation();
        if (annotation.key().isBlank()) {
            throw invalid("A translation unit requires an explicit stable key");
        }
        var schema = schema(property.type(), annotation, state.ancestors());
        state.fields()
                .add(new LocalizableField(
                        annotation.key(), path, schema.format(), schema, emptyToNull(annotation.group())));
        collectCatalogField(property, annotation.key(), schema, state);
    }

    private static void collectCatalogField(
            Property property, String key, LocalizableSchema schema, CollectionState state) {
        if (!property.titleField() && !property.summaryField()) {
            return;
        }
        validateCatalogField(schema);
        if (property.titleField()) {
            state.titleFields().add(key);
        }
        if (property.summaryField()) {
            state.summaryFields().add(key);
        }
    }

    private LocalizableSchema schema(JavaType type, Localizable annotation, Set<Class<?>> ancestors) {
        var builder = valueSchema(type, annotation.type(), ancestors);
        if (annotation.type() == LocalizableSchema.Type.ARRAY) {
            if (annotation.itemType() == LocalizableSchema.Type.ARRAY) {
                throw invalid("Directly nested array items require a programmatic schema");
            }
            var item = valueSchema(type.getContentType(), annotation.itemType(), ancestors);
            policy(item, annotation.itemPolicy());
            builder.items(item.build())
                    .identityField(emptyToNull(annotation.identityField()))
                    .independentStructure(annotation.independentStructure());
        }
        policy(builder, annotation.policy());
        return builder.label(emptyToNull(annotation.label()))
                .format(annotation.format())
                .required(annotation.required())
                .multiline(annotation.multiline())
                .nullable(annotation.nullable())
                .build();
    }

    private LocalizableSchema.Builder valueSchema(JavaType type, LocalizableSchema.Type kind, Set<Class<?>> ancestors) {
        if (type == null || type.hasRawClass(Object.class)) {
            throw invalid("Localization requires a concrete serialized value type");
        }
        return switch (kind) {
            case TEXT -> textual(type, LocalizableSchema.text());
            case IMAGE -> textual(type, LocalizableSchema.image());
            case LINK -> textual(type, LocalizableSchema.link());
            case BOOLEAN -> scalar(type, Boolean.class, boolean.class, LocalizableSchema.bool());
            case NUMBER -> numeric(type);
            case OBJECT -> objectSchema(type, ancestors);
            case ARRAY -> {
                if (!type.isCollectionLikeType() && !type.isArrayType()) {
                    throw invalid("Array declarations require a collection or array");
                }
                yield LocalizableSchema.array(LocalizableSchema.object());
            }
        };
    }

    private LocalizableSchema.Builder objectSchema(JavaType type, Set<Class<?>> ancestors) {
        enter(type, ancestors);
        var builder = LocalizableSchema.object();
        for (var property : properties(type)) {
            if (property.identity()) {
                throw invalid("Use a preserved property and identityField for array item identities");
            }
            if (property.titleField() || property.summaryField()) {
                throw invalid("Title and summary markers require a top-level localized property");
            }
            if (property.nested()) {
                builder.property(property.definition().getName(), objectSchema(property.type(), ancestors));
            }
            if (property.annotation() != null) {
                builder.property(
                        property.definition().getName(), schema(property.type(), property.annotation(), ancestors));
            }
        }
        ancestors.remove(type.getRawClass());
        return builder;
    }

    private List<Property> properties(JavaType type) {
        var description = mapper.getSerializationConfig().introspect(type);
        rejectSerializer(description.getClassInfo());
        if (description.findJsonValueAccessor() != null) {
            throw invalid("JsonValue types require a programmatic descriptor with an explicit object codec");
        }
        var definitions = description.findProperties();
        var ignored = mapper.getSerializationConfig()
                .getDefaultPropertyIgnorals(type.getRawClass(), description.getClassInfo());
        var excluded = ignored == null ? Set.<String>of() : ignored.findIgnoredForSerialization();
        var includes = description.getClassInfo().getAnnotation(JsonIncludeProperties.class);
        var included = includes == null ? null : Set.of(includes.value());
        var writable = mapper.getDeserializationConfig().introspect(type).findProperties().stream()
                .filter(BeanPropertyDefinition::couldDeserialize)
                .map(BeanPropertyDefinition::getName)
                .collect(java.util.stream.Collectors.toSet());
        var readable = definitions.stream()
                .filter(BeanPropertyDefinition::couldSerialize)
                .filter(property -> writable.contains(property.getName()))
                .filter(property -> !excluded.contains(property.getName()))
                .filter(property -> included == null || included.contains(property.getName()))
                .toList();
        var mappedMembers = new HashSet<Member>();
        readable.forEach(
                property -> members(property).map(AnnotatedMember::getMember).forEach(mappedMembers::add));
        validateVisibleMembers(type.getRawClass(), mappedMembers);
        return readable.stream()
                .map(property -> property(property, type.getRawClass()))
                .filter(Objects::nonNull)
                .toList();
    }

    private Property property(BeanPropertyDefinition definition, Class<?> rootType) {
        var markers = markers(definition, rootType);
        if (!markers.declared()) {
            return null;
        }
        validateMarkers(markers);
        var member = writableMember(definition);
        return new Property(
                definition,
                member.getType(),
                markers.annotation(),
                markers.nested(),
                markers.identity(),
                markers.link(),
                markers.titleField(),
                markers.summaryField());
    }

    private static Markers markers(BeanPropertyDefinition definition, Class<?> rootType) {
        return new Markers(
                annotation(definition, rootType, Localizable.class),
                annotation(definition, rootType, LocalizableNested.class) != null,
                annotation(definition, rootType, LocalizationId.class) != null,
                annotation(definition, rootType, LocalizableLink.class) != null,
                annotation(definition, rootType, LocalizableSearchTitle.class) != null,
                annotation(definition, rootType, LocalizableSearchSummary.class) != null);
    }

    private static void validateMarkers(Markers markers) {
        if (markers.titleField() && markers.summaryField()) {
            throw invalid("A localized property cannot be both a title and a summary");
        }
        if (markers.catalog() && !markers.localizedOnly()) {
            throw invalid("Title and summary markers require a localized field");
        }
        if (markers.link()
                && (markers.annotation() != null || markers.nested() || markers.identity() || markers.catalog())) {
            throw invalid("Public links cannot be translated or nested localization fields");
        }
        if (markers.conflicting()) {
            throw invalid("A property cannot have conflicting localization declarations");
        }
    }

    private static AnnotatedMember writableMember(BeanPropertyDefinition definition) {
        var member = definition.getAccessor();
        if (member == null || !definition.couldDeserialize()) {
            throw invalid("A localized property must be readable and writable by Jackson");
        }
        var unwrapped = member.getAnnotation(JsonUnwrapped.class);
        if (unwrapped != null && unwrapped.enabled()) {
            throw invalid("Unwrapped properties require a programmatic descriptor");
        }
        rejectSerializer(member);
        return member;
    }

    private static <A extends Annotation> A annotation(
            BeanPropertyDefinition property, Class<?> rootType, Class<A> type) {
        var annotations = annotatedElements(property, rootType)
                .map(member -> member.getAnnotation(type))
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (annotations.size() > 1) {
            throw invalid("Conflicting localization annotations on one serialized property");
        }
        return annotations.isEmpty() ? null : annotations.getFirst();
    }

    private static Stream<AnnotatedMember> members(BeanPropertyDefinition property) {
        return Stream.of(property.getField(), property.getGetter(), property.getSetter())
                .filter(Objects::nonNull);
    }

    private static Stream<AnnotatedElement> annotatedElements(BeanPropertyDefinition property, Class<?> rootType) {
        var mapped = members(property).map(AnnotatedMember::getMember).toList();
        var inherited = interfaceMembers(rootType)
                .filter(member -> mapped.stream().anyMatch(candidate -> sameMethod(candidate, member)))
                .map(member -> (AnnotatedElement) member);
        return Stream.concat(mapped.stream().map(member -> (AnnotatedElement) member), inherited);
    }

    private static void validateVisibleMembers(Class<?> type, Set<Member> mapped) {
        validateVisibleMembers(type, mapped, new HashSet<>());
    }

    private static void validateVisibleMembers(Class<?> type, Set<Member> mapped, Set<Class<?>> visited) {
        if (type == null || type == Object.class) {
            return;
        }
        if (!visited.add(type)) {
            return;
        }
        declaredMembers(type)
                .filter(member -> declared(member.annotationTarget()))
                .filter(member -> mapped.stream().noneMatch(candidate -> sameMethod(candidate, member.member())))
                .findAny()
                .ifPresent(member -> {
                    throw invalid("A localization annotation is on an ignored or non-serialized property");
                });
        Arrays.stream(type.getInterfaces()).forEach(contract -> validateVisibleMembers(contract, mapped, visited));
        validateVisibleMembers(type.getSuperclass(), mapped, visited);
    }

    private static boolean hasDeclarations(Class<?> type) {
        return hasDeclarations(type, new HashSet<>());
    }

    private static boolean hasDeclarations(Class<?> type, Set<Class<?>> visited) {
        if (type == null || type == Object.class) {
            return false;
        }
        if (!visited.add(type)) {
            return false;
        }
        var declared = declaredMembers(type).anyMatch(member -> declared(member.annotationTarget()));
        if (declared) {
            return true;
        }
        if (Arrays.stream(type.getInterfaces()).anyMatch(contract -> hasDeclarations(contract, visited))) {
            return true;
        }
        return hasDeclarations(type.getSuperclass(), visited);
    }

    private static Stream<Member> interfaceMembers(Class<?> type) {
        var interfaces = new HashSet<Class<?>>();
        for (var current = type; current != null && current != Object.class; current = current.getSuperclass()) {
            collectInterfaces(current, interfaces);
        }
        return interfaces.stream()
                .flatMap(contract -> Stream.concat(
                        Arrays.stream(contract.getDeclaredFields()), Arrays.stream(contract.getDeclaredMethods())));
    }

    private static Stream<DeclaredMember> declaredMembers(Class<?> type) {
        return Stream.concat(
                Arrays.stream(type.getDeclaredFields()).map(field -> new DeclaredMember(field, field)),
                Arrays.stream(type.getDeclaredMethods()).map(method -> new DeclaredMember(method, method)));
    }

    private static void collectInterfaces(Class<?> type, Set<Class<?>> interfaces) {
        for (var contract : type.getInterfaces()) {
            if (!interfaces.add(contract)) {
                continue;
            }
            collectInterfaces(contract, interfaces);
        }
    }

    private static boolean sameMethod(Member left, Member right) {
        if (left.equals(right)) {
            return true;
        }
        if (!(left instanceof Method first) || !(right instanceof Method second)) {
            return false;
        }
        return first.getName().equals(second.getName())
                && Arrays.equals(first.getParameterTypes(), second.getParameterTypes());
    }

    private static boolean declared(AnnotatedElement member) {
        return member.isAnnotationPresent(Localizable.class)
                || member.isAnnotationPresent(LocalizableNested.class)
                || member.isAnnotationPresent(LocalizationId.class)
                || member.isAnnotationPresent(LocalizableLink.class)
                || member.isAnnotationPresent(LocalizableSearchTitle.class)
                || member.isAnnotationPresent(LocalizableSearchSummary.class);
    }

    private static void rejectSerializer(com.fasterxml.jackson.databind.introspect.Annotated annotated) {
        var serializer = annotated.getAnnotation(JsonSerialize.class);
        if (serializer == null) {
            return;
        }
        if (serializer.using() != com.fasterxml.jackson.databind.JsonSerializer.None.class
                || serializer.converter() != com.fasterxml.jackson.databind.util.Converter.None.class
                || serializer.contentUsing() != com.fasterxml.jackson.databind.JsonSerializer.None.class
                || serializer.contentConverter() != com.fasterxml.jackson.databind.util.Converter.None.class) {
            throw invalid("Custom serialization requires a programmatic descriptor");
        }
    }

    private static void validateIdentity(Property property) {
        var raw = property.type().getRawClass();
        if (raw == String.class
                || raw == Byte.class
                || raw == Short.class
                || raw == Integer.class
                || raw == Long.class
                || raw == java.math.BigInteger.class) {
            return;
        }
        if (raw == byte.class || raw == short.class || raw == int.class || raw == long.class) {
            return;
        }
        throw invalid("Localization identities must be text or integers");
    }

    private static void validateLink(Property property) {
        if (!property.type().hasRawClass(String.class)) {
            throw invalid("Public link declarations require a string property");
        }
    }

    private static LocalizableSchema.Builder textual(JavaType type, LocalizableSchema.Builder schema) {
        if (!type.hasRawClass(String.class)) {
            throw invalid("Text, image and link declarations require a string property");
        }
        return schema;
    }

    private static LocalizableSchema.Builder numeric(JavaType type) {
        var raw = type.getRawClass();
        if (Number.class.isAssignableFrom(raw) || (raw.isPrimitive() && raw != boolean.class && raw != char.class)) {
            return LocalizableSchema.number();
        }
        throw invalid("Number declarations require a numeric property");
    }

    private static LocalizableSchema.Builder scalar(
            JavaType type, Class<?> boxed, Class<?> primitive, LocalizableSchema.Builder schema) {
        if (!type.hasRawClass(boxed) && !type.hasRawClass(primitive)) {
            throw invalid("The declared schema does not match the serialized value type");
        }
        return schema;
    }

    private static void policy(LocalizableSchema.Builder schema, Localizable.Operation operation) {
        if (operation != Localizable.Operation.DEFAULT) {
            schema.policy(LocalizableSchema.Policy.valueOf(operation.name()));
        }
    }

    private static void enter(JavaType type, Set<Class<?>> ancestors) {
        if (type.isContainerType() || type.isPrimitive() || type.hasRawClass(String.class)) {
            throw invalid("Nested declarations require an object with declared properties");
        }
        if (ancestors.size() >= 12 || !ancestors.add(type.getRawClass())) {
            throw invalid("Recursive or excessively deep localization declarations are not supported");
        }
    }

    private static void validateFields(List<LocalizableField> fields) {
        if (fields.stream().map(LocalizableField::key).distinct().count() != fields.size()) {
            throw invalid("Duplicate localization field keys");
        }
    }

    private static void validateLinkPointers(List<LocalizableField> fields, List<String> links) {
        var pointers = new HashSet<String>();
        for (var link : links) {
            if (!pointers.add(link)) {
                throw invalid("Duplicate public link pointers");
            }
            for (var field : fields) {
                var pointer = field.pointer();
                if (pointer != null
                        && (pointer.equals(link)
                                || pointer.startsWith(link + POINTER_SEPARATOR)
                                || link.startsWith(pointer + POINTER_SEPARATOR))) {
                    throw invalid("Localized fields cannot overlap public links");
                }
            }
        }
    }

    private static String catalogField(List<String> fields, String metadataName) {
        var keys = fields.stream().distinct().toList();
        if (keys.size() > 1) {
            throw invalid("A resource must declare at most one " + metadataName + " field");
        }
        return keys.isEmpty() ? null : keys.getFirst();
    }

    private static void validateCatalogField(LocalizableSchema schema) {
        if (schema.type() != LocalizableSchema.Type.TEXT || schema.format() != LocalizableField.Format.PLAIN_TEXT) {
            throw invalid("Localized search title and summary fields must be plain text");
        }
    }

    private static String escape(String value) {
        return value.replace("~", POINTER_TILDE_ESCAPE).replace(POINTER_SEPARATOR, POINTER_SLASH_ESCAPE);
    }

    private static String emptyToNull(String value) {
        return value.isBlank() ? null : value;
    }

    private static IllegalArgumentException invalid(String message) {
        return new IllegalArgumentException(message);
    }

    private record CollectionState(
            List<LocalizableField> fields,
            List<String> identities,
            List<String> linkPointers,
            List<String> titleFields,
            List<String> summaryFields,
            Set<Class<?>> ancestors) {}

    private record Markers(
            Localizable annotation,
            boolean nested,
            boolean identity,
            boolean link,
            boolean titleField,
            boolean summaryField) {
        boolean declared() {
            return annotation != null || nested || identity || link || titleField || summaryField;
        }

        boolean catalog() {
            return titleField || summaryField;
        }

        boolean localizedOnly() {
            return annotation != null && !nested && !identity;
        }

        boolean conflicting() {
            return (annotation != null && nested) || (identity && (annotation != null || nested || link));
        }
    }

    private record DeclaredMember(Member member, AnnotatedElement annotationTarget) {}

    private record Property(
            BeanPropertyDefinition definition,
            JavaType type,
            Localizable annotation,
            boolean nested,
            boolean identity,
            boolean link,
            boolean titleField,
            boolean summaryField) {}

    private record Declaration(
            List<LocalizableField> fields,
            List<String> identities,
            List<String> linkPointers,
            String titleField,
            String summaryField) {}
}
