package run.halo.app.localization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class LocalizableResourceDescriptorsTest {
    final JsonMapper mapper = JsonMapper.builder().build();

    @Test
    void resolvesSerializedIdentityAndStableKeysWithoutExtensionStorage() {
        var descriptor = LocalizableResourceDescriptors.fromAnnotations(Notice.class, mapper);
        var source = new Notice(42, "Hello");
        assertThat(descriptor.reference(source)).isEqualTo(ResourceReference.external("example", "notice", "42"));
        assertThat(descriptor.fields().getFirst().pointer()).isEqualTo("/heading");
        assertThat(descriptor.titleField()).isEqualTo("title");
    }

    @Test
    void rejectsMultipleFieldsWithTheSameCatalogRole() {
        assertThatThrownBy(() -> LocalizableResourceDescriptors.fromAnnotations(DuplicateTitle.class, mapper))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at most one title");
    }

    @Test
    void rejectsCatalogRolesOnNonTextFields() {
        assertThatThrownBy(() -> LocalizableResourceDescriptors.fromAnnotations(InvalidSummary.class, mapper))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("plain text");
    }

    @Test
    void capturesPublicLinksWithoutTurningThemIntoTranslationFields() {
        var descriptor = LocalizableResourceDescriptors.fromAnnotations(LinkedItem.class, mapper);

        assertThat(descriptor.linkPointers()).containsExactly("/destination");
        assertThat(descriptor.fields()).extracting(LocalizableField::key).containsExactly("heading");
    }

    @Test
    void capturesOneCodecForDeclarationsIdentityAndDetachedValues() {
        var customMapper = JsonMapper.builder()
                .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .build();
        var descriptor = LocalizableResourceDescriptors.fromAnnotations(DisplayItem.class, customMapper);
        customMapper.setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);
        var source = new DisplayItem(42, "Hello", new BigDecimal("15.00"));

        var tree = descriptor.toJson(source);

        assertThat(descriptor.reference(source)).isEqualTo(ResourceReference.external("example", "item", "42"));
        assertThat(descriptor.fields().getFirst().pointer()).isEqualTo("/display_name");
        assertThat(tree.get("record_id").asLong()).isEqualTo(42);
        assertThat(tree.get("unit_price").decimalValue()).isEqualTo(new BigDecimal("15.00"));
        tree.put("display_name", "Translated");
        assertThat(descriptor.fromJson(tree)).isEqualTo(new DisplayItem(42, "Translated", new BigDecimal("15.00")));
        assertThat(source.displayName()).isEqualTo("Hello");
        assertThat(descriptor.toJson(source).get("display_name").asText()).isEqualTo("Hello");
    }

    @Test
    void rejectsJsonValueShapesInsteadOfCompilingPointersToAbsentProperties() {
        assertThatThrownBy(() -> LocalizableResourceDescriptors.fromAnnotations(ScalarResource.class, mapper))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("JsonValue");
        assertThatThrownBy(() -> LocalizableResourceDescriptors.fromAnnotations(NestedScalarResource.class, mapper))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("JsonValue");
    }

    @Test
    void roundTripsBothIdentityKindsWithoutConvertingExternalIdsToGvk() throws Exception {
        var external = ResourceReference.external("example", "notice", "42");
        var encoded = mapper.writeValueAsString(external);
        assertThat(mapper.readValue(encoded, ResourceReference.class)).isEqualTo(external);
        assertThat(mapper.readTree(encoded).get("gvk").isNull()).isTrue();
        var extension = new ResourceReference(run.halo.app.core.extension.content.Post.GVK, "42");
        assertThat(mapper.readValue(mapper.writeValueAsString(extension), ResourceReference.class))
                .isEqualTo(extension);
    }

    @LocalizableResource(namespace = "example", type = "notice")
    record Notice(
            @LocalizationId @JsonProperty("key") long id,

            @LocalizableSearchTitle @Localizable(key = "title") @JsonProperty("heading")
            String label) {}

    @LocalizableResource(namespace = "example", type = "duplicate-title")
    record DuplicateTitle(
            @LocalizationId String id,

            @LocalizableSearchTitle @Localizable(key = "title")
            String title,

            @LocalizableSearchTitle @Localizable(key = "otherTitle")
            String otherTitle) {}

    @LocalizableResource(namespace = "example", type = "invalid-summary")
    record InvalidSummary(
            @LocalizationId String id,

            @LocalizableSearchSummary @Localizable(key = "summary", type = LocalizableSchema.Type.IMAGE)
            String summary) {}

    @LocalizableResource(namespace = "example", type = "linked-item")
    record LinkedItem(
            @LocalizationId String id,

            @Localizable(key = "heading") String title,

            @LocalizableLink @JsonProperty("destination") String link) {}

    @LocalizableResource(namespace = "example", type = "item")
    record DisplayItem(
            @LocalizationId long recordId,
            @Localizable(key = "heading") String displayName,
            BigDecimal unitPrice) {}

    @LocalizableResource(namespace = "example", type = "scalar")
    record ScalarResource(
            @LocalizationId String id,
            @Localizable(key = "heading") String title) {
        @JsonValue
        public String value() {
            return title;
        }
    }

    @LocalizableResource(namespace = "example", type = "nested-scalar")
    record NestedScalarResource(
            @LocalizationId String id,

            @Localizable(key = "items", type = LocalizableSchema.Type.ARRAY)
            List<ScalarResource> items) {}
}
