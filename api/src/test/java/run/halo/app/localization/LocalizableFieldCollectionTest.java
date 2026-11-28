package run.halo.app.localization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.List;
import org.junit.jupiter.api.Test;
import run.halo.app.extension.GroupVersionKind;

class LocalizableFieldCollectionTest {
    private final JsonMapper mapper = JsonMapper.builder().build();
    private final LocalizableFieldCollection collection = new LocalizableFieldCollection(
            "metas", "/spec/items", List.of("name", "property"), "content", LocalizableField.Format.PLAIN_TEXT);

    @Test
    void groupedDeclarationsRegisterIdentityPropertiesAndResolveInSourceOrder() throws Exception {
        var declared = LocalizableFieldCollection.builder("metas", "/spec/items")
                .valueField("content")
                .group("Search")
                .entries(
                        "name",
                        entries -> entries.add(
                                        "description",
                                        LocalizableSchema.text()
                                                .label("Description")
                                                .multiline(true))
                                .add("keywords", LocalizableSchema.text().label("Keywords")))
                .entries(
                        "property",
                        entries -> entries.add(
                                "description", LocalizableSchema.image().label("Image")))
                .build();
        var source = mapper.readTree("""
            {"spec":{"items":[
              {"property":"description","content":"/share.png"},
              {"name":"description","content":"Description"},
              {"name":"unknown","content":"Unspecified"},
              {"name":"description","property":"description","content":"Ambiguous"}
            ]}}
            """);

        assertThat(declared.identityFields()).containsExactly("name", "property");
        var fields = declared.resolve(source);
        assertThat(fields)
                .extracting(LocalizableField::key)
                .containsExactly(
                        declared.fieldKey("property", "description"),
                        declared.fieldKey("name", "description"),
                        declared.fieldKey("name", "unknown"));
        assertThat(fields).extracting(LocalizableField::group).containsOnly("Search");
        assertThat(fields.getFirst().schema().type()).isEqualTo(LocalizableSchema.Type.IMAGE);
        assertThat(fields.get(1).schema().multiline()).isTrue();
        assertThat(fields.get(1).schema().supportsGeneration()).isTrue();
        assertThat(fields.getLast().schema().policy()).isEqualTo(LocalizableSchema.Policy.REPLACE);
        assertThat(fields.getLast().schema().supportsGeneration()).isFalse();
    }

    @Test
    void referenceSemanticsFollowDeclarationsAcrossNamesAndIdentityReordering() throws Exception {
        var image = LocalizableSchema.image().label("Image").build();
        var link = LocalizableSchema.link().label("Link").build();
        var preserved =
                link.toBuilder().policy(LocalizableSchema.Policy.PRESERVE).build();
        var declared = LocalizableFieldCollection.builder("references", "/spec/items")
                .valueField("payload")
                .entries(
                        "identity",
                        entries -> entries.add("description", image)
                                .add("other-image", image)
                                .add("cover", link)
                                .add("other-link", link)
                                .add("fixed", preserved))
                .build();
        var source = mapper.readTree("""
            {"spec":{"items":[
              {"identity":"description","payload":"/source.png"},
              {"identity":"other-image","payload":"/other.png"},
              {"identity":"cover","payload":"/source"},
              {"identity":"other-link","payload":"/other"},
              {"identity":"fixed","payload":"/fixed"}
            ]}}
            """);
        var original = source.deepCopy();
        var fields = declared.resolve(source);

        assertThat(fields).extracting(LocalizableField::schema).containsExactly(image, image, link, link, preserved);
        assertThat(fields).allMatch(field -> !field.schema().supportsGeneration());
        assertThat(source).isEqualTo(original);

        var reordered = source.deepCopy();
        var items = (ArrayNode) reordered.at("/spec/items");
        items.add(items.remove(0));
        var moved = declared.resolve(reordered).getLast();
        assertThat(moved.key()).isEqualTo(fields.getFirst().key());
        assertThat(moved.pointer()).isEqualTo("/spec/items/4/payload");
        assertThat(moved.schema()).isEqualTo(image);
        assertThat(reordered.at(moved.pointer()).asText()).isEqualTo("/source.png");
        assertThat(source).isEqualTo(original);
    }

    @Test
    void fallbackFormatAppliesOnlyToUnknownEntriesWithoutEnablingGeneration() throws Exception {
        var declared = LocalizableFieldCollection.builder("metas", "/spec/items")
                .valueField("content")
                .identityFields("name", "property")
                .fallbackFormat(LocalizableField.Format.HTML)
                .entries("name", entries -> entries.add("title", LocalizableSchema.text()))
                .build();
        var fields = declared.resolve(mapper.readTree("""
            {"spec":{"items":[{"name":"title","content":"Title"},
              {"property":"unknown","content":"<p>Source</p>"}]}}
            """));

        assertThat(declared.identityFields()).containsExactly("name", "property");
        assertThat(fields.getFirst().format()).isEqualTo(LocalizableField.Format.PLAIN_TEXT);
        assertThat(fields.getLast().format()).isEqualTo(LocalizableField.Format.HTML);
        assertThat(fields.getLast().schema().supportsGeneration()).isFalse();
        assertThat(declared.schemaFor("property", "unknown")).isEmpty();
    }

    @Test
    void completedGroupsAndBuiltCollectionsDoNotRetainMutableBuilderState() {
        var schema = LocalizableSchema.text().label("Original");
        var captured = new java.util.concurrent.atomic.AtomicReference<LocalizableFieldCollection.Entries>();
        var builder = LocalizableFieldCollection.builder("metas", "/spec/items")
                .valueField("content")
                .entries("name", entries -> {
                    entries.add("title", schema);
                    captured.set(entries);
                });
        var first = builder.build();
        schema.label("Changed");
        captured.get().add("late", LocalizableSchema.text());
        builder.entries(
                "property",
                entries -> entries.add("description", LocalizableSchema.text().build()));
        var second = builder.build();

        assertThat(first.identityFields()).containsExactly("name");
        assertThat(second.identityFields()).containsExactly("name", "property");
        assertThat(first.definitions()).hasSize(1);
        assertThat(second.definitions()).hasSize(2);
        assertThat(second.schemaFor("name", "late")).isEmpty();
        assertThat(second.schemaFor("name", "title").orElseThrow().label()).isEqualTo("Original");
        assertThatThrownBy(() -> first.definitions().clear()).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void groupedDeclarationsKeepIdentityAndSchemaValidation() {
        assertThatThrownBy(() -> LocalizableFieldCollection.builder("metas", "/spec/items")
                        .valueField("content")
                        .entries("name", entries -> entries.add("title", LocalizableSchema.text()))
                        .entries("name", entries -> entries.add("title", LocalizableSchema.image()))
                        .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Duplicate");
        assertThatThrownBy(() -> LocalizableFieldCollection.builder("metas", "/spec/items")
                        .valueField("name")
                        .identityFields("name")
                        .build())
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> LocalizableFieldCollection.builder("metas", "/spec/items")
                        .valueField("content")
                        .entries("name", entries -> entries.add("title", LocalizableSchema.object()))
                        .build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void stableKeysRetainIdentityCaseAndSeparateIdentityAttributes() throws Exception {
        var source = mapper.readTree("""
                {"spec":{"items":[
                  {"name":"Description","content":"A"},
                  {"property":"Description","content":"B"},
                  {"name":"description","content":"C"}
                ]}}
                """);
        var first = collection.resolve(source);
        var array = (ArrayNode) source.at("/spec/items");
        var entry = array.remove(0);
        array.add(entry);
        var reordered = collection.resolve(source);
        assertThat(reordered)
                .extracting(LocalizableField::key)
                .containsExactly(
                        first.get(1).key(), first.get(2).key(), first.get(0).key());
        assertThat(reordered.getLast().pointer()).isEqualTo("/spec/items/2/content");
        assertThat(collection.fieldKey("name", "a/b~c")).isNotEqualTo(collection.fieldKey("name", "a~1b~0c"));
    }

    @Test
    void malformedAndDuplicateIdentitiesCannotSelectAnAdjacentEntry() throws Exception {
        var source = mapper.readTree("""
                {"spec":{"items":[
                  {"name":"duplicate","content":"A"},
                  {"name":"duplicate","content":null},
                  {"name":"ambiguous","property":"another","content":"B"},
                  {"content":"C"},
                  {"name":123,"content":"D"},
                  {"name":"","content":"E"},
                  {"name":"number","content":12},
                  {"name":"valid","content":"F"}
                ]}}
                """);
        assertThat(collection.resolve(source))
                .containsExactly(new LocalizableField(
                        collection.fieldKey("name", "valid"),
                        "/spec/items/7/content",
                        LocalizableField.Format.PLAIN_TEXT,
                        LocalizableSchema.text()
                                .policy(LocalizableSchema.Policy.REPLACE)
                                .build(),
                        null));
        ((ArrayNode) source.at("/spec/items")).remove(7);
        assertThat(collection.resolve(source)).isEmpty();
        assertThat(collection.resolve(mapper.readTree("{\"spec\":{\"items\":{}}}")))
                .isEmpty();
    }

    @Test
    void escapedValuePropertiesResolveWithoutEscapingTheAllowedCollection() throws Exception {
        var escaped = new LocalizableFieldCollection(
                "labels", "/spec/items", List.of("id"), "text/~value", LocalizableField.Format.PLAIN_TEXT);
        var source = mapper.readTree("""
                {"spec":{"items":[{"id":"a","text/~value":"hello"}]}}
                """);
        var field = escaped.resolve(source).getFirst();
        assertThat(field.pointer()).isEqualTo("/spec/items/0/text~1~0value");
        assertThat(source.at(field.pointer()).asText()).isEqualTo("hello");
        assertThatThrownBy(() -> new LocalizableFieldCollection(
                        "invalid", "labels", List.of("name"), "value", LocalizableField.Format.PLAIN_TEXT))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new LocalizableFieldCollection(
                        "invalid", "/spec/items", List.of("name"), "name", LocalizableField.Format.PLAIN_TEXT))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void staticBindingsAndAmbiguousDeclarationsAreIsolated() throws Exception {
        var source = mapper.readTree("""
                {"spec":{"items":[{"name":"description","content":"hello"}]}}
                """);
        var dynamic = collection.resolve(source).getFirst();
        var staticField = new LocalizableField("static", dynamic.pointer(), LocalizableField.Format.PLAIN_TEXT);
        assertThat(descriptor(List.of(staticField), List.of(collection)).resolveDisplayFields(source))
                .containsExactly(staticField);
        var collision = new LocalizableField(dynamic.key(), "/spec/title", LocalizableField.Format.PLAIN_TEXT);
        assertThat(descriptor(List.of(collision), List.of(collection)).resolveDisplayFields(source))
                .containsExactly(collision);
        assertThat(descriptor(List.of(), List.of(collection, collection)).resolveDisplayFields(source))
                .isEmpty();
        var alias = new LocalizableFieldCollection(
                "alias",
                collection.pointer(),
                collection.identityFields(),
                collection.valueField(),
                collection.fallbackFormat());
        assertThat(descriptor(List.of(), List.of(collection, alias)).resolveDisplayFields(source))
                .isEmpty();
    }

    @Test
    void wholeStructuredValuesCannotCompeteWithDescendantOrDuplicatePointers() throws Exception {
        var source = mapper.readTree("""
            {"spec":{"items":[{"name":"description","content":"hello"}],"itemsExtra":"keep"}}
            """);
        var whole = LocalizableField.array(
                        "whole",
                        "/spec/items",
                        LocalizableSchema.object().property("content", LocalizableSchema.text()))
                .build();
        var child = LocalizableField.text("child", "/spec/items/0/content").build();
        var adjacent = LocalizableField.text("adjacent", "/spec/itemsExtra").build();
        assertThat(descriptor(List.of(whole, child, adjacent), List.of()).resolveDisplayFields(source))
                .containsExactly(adjacent);
        assertThat(descriptor(List.of(whole, adjacent), List.of(collection)).resolveDisplayFields(source))
                .containsExactly(adjacent);
        var duplicate = LocalizableField.text("duplicate", adjacent.pointer()).build();
        assertThat(descriptor(List.of(adjacent, duplicate), List.of()).resolveDisplayFields(source))
                .isEmpty();
    }

    @Test
    void overlapUsesDecodedPointerSegments() throws Exception {
        var source = mapper.readTree("{\"spec\":{\"outer/inner\":{\"text\":\"hello\"},\"empty\":\"\"}}");
        var object = LocalizableField.object(
                        "object",
                        "/spec/outer~1inner",
                        LocalizableSchema.object()
                                .property("text", LocalizableSchema.text())
                                .build())
                .build();
        var child = LocalizableField.text("child", "/spec/outer~1inner/text").build();
        assertThat(descriptor(List.of(object, child), List.of()).resolveDisplayFields(source))
                .isEmpty();
    }

    private ResourceLocalizationDescriptor descriptor(
            List<LocalizableField> fields, List<LocalizableFieldCollection> collections) {
        return new ResourceLocalizationDescriptor() {
            @Override
            public GroupVersionKind supports() {
                return new GroupVersionKind("example.com", "v1", "Example");
            }

            @Override
            public List<LocalizableField> fields() {
                return fields;
            }

            @Override
            public List<LocalizableFieldCollection> fieldCollections() {
                return collections;
            }
        };
    }
}
