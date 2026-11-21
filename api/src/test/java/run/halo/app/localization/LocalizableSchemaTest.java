package run.halo.app.localization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.json.JsonMapper;
import java.util.List;
import org.junit.jupiter.api.Test;

class LocalizableSchemaTest {
    private final JsonMapper mapper = JsonMapper.builder().build();

    @Test
    void fluentDeclarationsDoNotInferBehaviorFromNamesOrValues() {
        var fields = LocalizableField.list(
                LocalizableField.text("cover", "/spec/arbitrary").label("Text"),
                LocalizableField.image("description", "/spec/another").label("Image"),
                LocalizableField.raw("source", null));
        assertThat(fields.getFirst().schema().supportsGeneration()).isTrue();
        assertThat(fields.get(1).schema().supportsGeneration()).isFalse();
        assertThat(fields.get(2).schema().supportsGeneration()).isFalse();
        LocalizableValues.validate(
                fields.getFirst().schema(), "https://example.com/x.png", "Translated URL discussion");
        assertThatThrownBy(() -> LocalizableValues.validate(fields.get(1).schema(), "/a.png", "javascript:alert(1)"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(fields).extracting(LocalizableField::key).containsExactly("cover", "description", "source");
    }

    @Test
    void structuredProjectionKeepsUnknownAndPreservedMembers() throws Exception {
        var schema = LocalizableSchema.object()
                .property("heading", LocalizableSchema.text())
                .property("enabled", LocalizableSchema.bool())
                .property("art", LocalizableSchema.image())
                .property("nested", LocalizableSchema.array(LocalizableSchema.text()))
                .build();
        var source = mapper.readTree("""
            {"heading":"Source","enabled":true,"art":"/source.png","nested":["One"],"unknown":42}
            """);
        var candidate = mapper.readTree("""
            {"heading":"Target","art":"/target.png","nested":["Translated"]}
            """);
        var result = LocalizableValues.project(schema, source, candidate);
        assertThat(result.path("enabled").asBoolean()).isTrue();
        assertThat(result.path("unknown").asInt()).isEqualTo(42);
        assertThat(result.path("nested").get(0).asText()).isEqualTo("Translated");
        assertThat(source.path("heading").asText()).isEqualTo("Source");
        assertThatThrownBy(() -> LocalizableValues.project(schema, source, mapper.readTree("{\"enabled\":false}")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> LocalizableValues.project(schema, source, mapper.readTree("{\"unknown\":43}")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void requiredPropertiesAreValidatedAfterMergingOmittedSourceValues() throws Exception {
        var schema = LocalizableSchema.object()
                .property("text", LocalizableSchema.text().required(true))
                .property("optional", LocalizableSchema.text())
                .build();
        var omitted = mapper.readTree("{}");
        for (var source : List.of("{}", "{\"text\":null}", "{\"text\":\"\"}", "{\"text\":\"  \"}")) {
            assertThatThrownBy(() -> LocalizableValues.project(schema, mapper.readTree(source), omitted))
                    .isInstanceOf(IllegalArgumentException.class);
        }
        var source = mapper.readTree("{\"text\":\"Source\",\"optional\":null}");
        assertThat(LocalizableValues.project(schema, source, omitted)).isEqualTo(source);
        assertThat(LocalizableValues.project(
                                schema,
                                mapper.readTree("{\"text\":null}"),
                                mapper.readTree("{\"text\":\"Translated\"}"))
                        .path("text")
                        .asText())
                .isEqualTo("Translated");
        assertThatThrownBy(() -> LocalizableValues.project(schema, source, mapper.readTree("{\"text\":null}")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void requiredValidationReachesPreservedAndOmittedNestedProperties() throws Exception {
        var nested = LocalizableSchema.object()
                .property("text", LocalizableSchema.text().required(true))
                .build();
        var schema = LocalizableSchema.object()
                .property("nested", nested)
                .property("items", LocalizableSchema.array(nested))
                .property("fixed", LocalizableSchema.text().required(true).policy(LocalizableSchema.Policy.PRESERVE))
                .build();
        var omitted = mapper.readTree("{}");
        for (var source : List.of(
                "{\"fixed\":null}",
                "{\"fixed\":\"\"}",
                "{\"fixed\":\"keep\",\"nested\":{\"text\":\"\"}}",
                "{\"fixed\":\"keep\",\"items\":[{\"text\":null}]}")) {
            assertThatThrownBy(() -> LocalizableValues.project(schema, mapper.readTree(source), omitted))
                    .isInstanceOf(IllegalArgumentException.class);
        }
        var source = mapper.readTree("{\"fixed\":\"keep\",\"nested\":null,\"items\":[]}");
        assertThat(LocalizableValues.project(schema, source, omitted)).isEqualTo(source);
    }

    @Test
    void arraysRequireExplicitIdentitiesForIndependentSourceOwnedItems() throws Exception {
        var item = LocalizableSchema.object()
                .property("stable", LocalizableSchema.text().policy(LocalizableSchema.Policy.PRESERVE))
                .property("text", LocalizableSchema.text())
                .build();
        var source = mapper.readTree("[{\"stable\":\"a\",\"text\":\"A\"},{\"stable\":\"b\",\"text\":\"B\"}]");
        var candidate = mapper.readTree("[{\"stable\":\"b\",\"text\":\"Bee\"},{\"stable\":\"a\",\"text\":\"Ay\"}]");
        var fixed = LocalizableSchema.array(item).identityField("stable").build();
        assertThatThrownBy(() -> LocalizableValues.project(fixed, source, candidate))
                .isInstanceOf(IllegalArgumentException.class);
        var independent = fixed.toBuilder().independentStructure(true).build();
        assertThat(LocalizableValues.project(independent, source, candidate)).isEqualTo(candidate);
        var noIdentity =
                LocalizableSchema.array(item).independentStructure(true).build();
        assertThatThrownBy(() -> LocalizableValues.project(noIdentity, source, candidate))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> LocalizableValues.project(
                        independent,
                        source,
                        mapper.readTree("[{\"stable\":\"a\",\"text\":\"A\"},{\"stable\":\"a\",\"text\":\"B\"}]")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void independentListsCanAddRemoveAndReorderDeclaredItems() throws Exception {
        var schema = LocalizableSchema.array(LocalizableSchema.text())
                .independentStructure(true)
                .build();
        var source = mapper.readTree("[\"one\",\"two\"]");
        var candidate = mapper.readTree("[\"two translated\",\"new\",\"one translated\"]");
        assertThat(LocalizableValues.project(schema, source, candidate)).isEqualTo(candidate);
        assertThat(LocalizableValues.project(schema, source, mapper.readTree("[]")))
                .isEmpty();
    }

    @Test
    void codecUsesDeclaredTypesAndRetainsNullEmptyAndNumericValues() throws Exception {
        var strings = LocalizableSchema.text().build();
        assertThat(LocalizableValues.decode(strings, "[not JSON]").asText()).isEqualTo("[not JSON]");
        var list = LocalizableSchema.array(strings).build();
        assertThat(LocalizableValues.encode(list, mapper.readTree("null"))).isEqualTo("null");
        assertThat(LocalizableValues.encode(list, mapper.readTree("[]"))).isEqualTo("[]");
        assertThat(LocalizableValues.decode(LocalizableSchema.number().build(), "42")
                        .isNumber())
                .isTrue();
        assertThat(LocalizableValues.decode(LocalizableSchema.bool().build(), "false")
                        .isBoolean())
                .isTrue();
        assertThatThrownBy(() -> LocalizableValues.decode(list, "{}")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() ->
                        LocalizableValues.decode(LocalizableSchema.object().build(), "{\"a\":1,\"a\":2}"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void declarationsAreImmutableAndRejectInvalidShapeAndIdentity() {
        var builder = LocalizableSchema.object().property("text", LocalizableSchema.text());
        var first = builder.build();
        builder.property("extra", LocalizableSchema.image());
        assertThat(first.properties()).containsOnlyKeys("text");
        assertThatThrownBy(() -> first.properties().clear()).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(
                        () -> LocalizableSchema.array(first).identityField("id").build())
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> LocalizableSchema.image()
                        .policy(LocalizableSchema.Policy.TRANSLATE)
                        .build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void collectionSemanticsComeFromExactDeclarationsOnly() throws Exception {
        var collection = new LocalizableFieldCollection(
                "anything",
                "/spec/items",
                List.of("key"),
                "value",
                LocalizableField.Format.PLAIN_TEXT,
                List.of(
                        new LocalizableFieldCollection.EntryDefinition(
                                "key", "cover", LocalizableSchema.text().build()),
                        new LocalizableFieldCollection.EntryDefinition(
                                "key", "arbitrary", LocalizableSchema.image().build())),
                "Group");
        var fields = collection.resolve(mapper.readTree("""
            {"spec":{"items":[{"key":"cover","value":"URL instructions"},
            {"key":"arbitrary","value":"/a.png"},{"key":"unknown","value":"text"}]}}
            """));
        assertThat(fields.get(0).schema().supportsGeneration()).isTrue();
        assertThat(fields.get(1).schema().type()).isEqualTo(LocalizableSchema.Type.IMAGE);
        assertThat(fields.get(2).schema().policy()).isEqualTo(LocalizableSchema.Policy.REPLACE);
        assertThat(fields).extracting(LocalizableField::group).containsOnly("Group");
    }

    @Test
    void malformedNestedSourceIsRejectedBeforeEditorsOrEnginesTraverseIt() throws Exception {
        var schema = LocalizableSchema.array(LocalizableSchema.object()
                        .property("children", LocalizableSchema.array(LocalizableSchema.text())))
                .build();
        var malformed = "[{\"children\":{\"wrong\":\"shape\"}}]";
        assertThatThrownBy(() -> LocalizableValues.decode(schema, malformed))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> LocalizableValues.encode(schema, mapper.readTree(malformed)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void schemaAndOpaqueSourceDepthAreBounded() throws Exception {
        var schema = LocalizableSchema.text().build();
        for (int i = 0; i < 12; i++) {
            schema = LocalizableSchema.array(schema).build();
        }
        var deepest = schema;
        assertThatThrownBy(() -> LocalizableSchema.array(deepest).build()).isInstanceOf(IllegalArgumentException.class);
        var object = LocalizableSchema.object().build();
        var source = mapper.readTree("{\"opaque\":" + "[".repeat(13) + "0" + "]".repeat(13) + "}");
        assertThatThrownBy(() -> LocalizableValues.project(object, source, mapper.readTree("{}")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void changedSchemaCannotAuthorizeOldOverridesOfRemovedOrPreservedProperties() throws Exception {
        var source = "{\"text\":\"A\",\"control\":\"keep\"}";
        var oldResult = "{\"text\":\"B\",\"control\":\"changed\"}";
        var removed = LocalizableSchema.object()
                .property("text", LocalizableSchema.text())
                .build();
        assertThatThrownBy(() -> LocalizableValues.validate(removed, source, oldResult))
                .isInstanceOf(IllegalArgumentException.class);
        var preserved = removed.toBuilder()
                .property("control", LocalizableSchema.text().policy(LocalizableSchema.Policy.PRESERVE))
                .build();
        assertThatThrownBy(() -> LocalizableValues.validate(preserved, source, oldResult))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void explicitIdentityKeepsPreservedValuesWithTheirItemAndChecksNewItems() throws Exception {
        var item = LocalizableSchema.object()
                .property("key", LocalizableSchema.text().policy(LocalizableSchema.Policy.PRESERVE))
                .property("text", LocalizableSchema.text())
                .property("fixed", LocalizableSchema.bool())
                .build();
        var schema = LocalizableSchema.array(item)
                .identityField("key")
                .independentStructure(true)
                .build();
        var source = mapper.readTree(
                "[{\"key\":\"a\",\"text\":\"A\",\"fixed\":true},{\"key\":\"b\",\"text\":\"B\",\"fixed\":false}]");
        var candidate = mapper.readTree(
                "[{\"key\":\"b\",\"text\":\"Bee\"},{\"key\":\"c\",\"text\":\"New\"},{\"key\":\"a\",\"text\":\"Ay\"}]");
        var result = LocalizableValues.project(schema, source, candidate);
        assertThat(result.at("/0/fixed").asBoolean()).isFalse();
        assertThat(result.at("/1/fixed").isMissingNode()).isTrue();
        assertThat(result.at("/2/fixed").asBoolean()).isTrue();
        assertThatThrownBy(() -> LocalizableValues.project(
                        schema, source, mapper.readTree("[{\"key\":\"c\",\"text\":\"New\",\"fixed\":true}]")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
