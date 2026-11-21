package run.halo.app.localization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.json.JsonMapper;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.Unstructured;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;

class StructuredLocalizationTest {
    private final JsonMapper mapper = JsonMapper.builder().build();
    private final LocalizationContext context = new LocalizationContext(
            Locale.CHINA, Locale.ENGLISH, List.of(Locale.FRENCH), LocalizationScope.PUBLIC_PAGE);
    private final LocalizableSchema schema = LocalizableSchema.array(LocalizableSchema.object()
                    .property("text", LocalizableSchema.text())
                    .property("artwork", LocalizableSchema.image())
                    .property("enabled", LocalizableSchema.bool()))
            .build();

    @Test
    void typedArrayProjectionPreservesTypesUnknownMembersAndOriginalResource() throws Exception {
        var source = source();
        var original = mapper.valueToTree(source).at("/spec/items");
        var candidate = "[{\"text\":\"Translated\",\"artwork\":\"/target.png\",\"enabled\":true,\"other\":7}]";
        var service = service(source, List.of(patch(source, Locale.ENGLISH, original.toString(), candidate)));
        var result = mapper.valueToTree(service.localize(source, context).block());
        assertThat(result.at("/spec/items").isArray()).isTrue();
        assertThat(result.at("/spec/items/0/text").asText()).isEqualTo("Translated");
        assertThat(result.at("/spec/items/0/enabled").isBoolean()).isTrue();
        assertThat(result.at("/spec/items/0/other").asInt()).isEqualTo(7);
        assertThat(mapper.valueToTree(source).at("/spec/items/0/text").asText()).isEqualTo("Source");
    }

    @Test
    void invalidPrimaryFallsBackWithoutApplyingUnsafeOrTypeChangingValues() {
        var source = source();
        var original = mapper.valueToTree(source).at("/spec/items").toString();
        var invalid = "[{\"text\":\"Wrong\",\"enabled\":false,\"artwork\":\"javascript:bad\"}]";
        var valid = "[{\"text\":\"French\",\"artwork\":\"/a.png\",\"enabled\":true,\"other\":7}]";
        var service = service(
                source,
                List.of(
                        patch(source, Locale.ENGLISH, original, invalid),
                        patch(source, Locale.FRENCH, original, valid)));
        assertThat(mapper.valueToTree(service.localize(source, context).block())
                        .at("/spec/items/0/text")
                        .asText())
                .isEqualTo("French");
    }

    @Test
    void semanticProjectionAlsoPreservesUndeclaredAndPreservedValues() throws Exception {
        var source = source();
        var original = mapper.valueToTree(source).at("/spec/items").toString();
        var service = service(source, List.of(patch(source, Locale.ENGLISH, original, "[{\"text\":\"Partial\"}]")));
        var values = service.localizeFields(
                        ResourceReference.from(source), Map.of("panels", original), List.of(), context)
                .block();
        var result = mapper.readTree(values.get("panels"));
        assertThat(result.at("/0/text").asText()).isEqualTo("Partial");
        assertThat(result.at("/0/artwork").asText()).isEqualTo("/a.png");
        assertThat(result.at("/0/enabled").asBoolean()).isTrue();
        assertThat(result.at("/0/other").asInt()).isEqualTo(7);
    }

    @Test
    void changedArrayRevisionNeverReusesOldPositionBasedTranslation() {
        var source = source();
        var service = service(
                source,
                List.of(patch(
                        source,
                        Locale.ENGLISH,
                        "[{\"text\":\"Older\",\"artwork\":\"/a.png\",\"enabled\":true,\"other\":7}]",
                        "[{\"text\":\"Wrong item\",\"artwork\":\"/a.png\",\"enabled\":true,\"other\":7}]")));
        assertThat(service.localize(source, context).block()).isSameAs(source);
    }

    @Test
    void scalarImageUsesSchemaRegardlessOfFieldKey() {
        var field = LocalizableField.image("not-cover", "/spec/artwork").build();
        assertThat(field.schema().type()).isEqualTo(LocalizableSchema.Type.IMAGE);
        assertThat(field.schema().supportsGeneration()).isFalse();
        assertThat(new LocalizableAnnotations(mapper)
                        .descriptor(Post.class).fields().stream()
                                .filter(item ->
                                        item.pointer() != null && item.pointer().equals("/spec/cover"))
                                .findFirst()
                                .orElseThrow()
                                .schema()
                                .type())
                .isEqualTo(field.schema().type());
    }

    private Unstructured source() {
        return new Unstructured(Map.of(
                "apiVersion",
                "example.com/v1",
                "kind",
                "Gallery",
                "metadata",
                Map.of("name", "gallery"),
                "spec",
                Map.of("items", List.of(Map.of("text", "Source", "artwork", "/a.png", "enabled", true, "other", 7)))));
    }

    private LocalizationPatch patch(Unstructured source, Locale locale, String before, String after) {
        return new LocalizationPatch(
                ResourceReference.from(source),
                locale,
                List.of(new LocalizationPatch.Field(
                        "panels", LocalizationHash.of(before), after, LocalizationPatch.State.APPROVED)));
    }

    private DefaultResourceLocalizationService service(Unstructured source, List<LocalizationPatch> patches) {
        var getter = mock(ExtensionGetter.class);
        var provider = mock(ResourceLocalizationProvider.class);
        var descriptor = new ResourceLocalizationDescriptor() {
            @Override
            public GroupVersionKind supports() {
                return source.groupVersionKind();
            }

            @Override
            public List<LocalizableField> fields() {
                return List.of(new LocalizableField(
                        "panels", "/spec/items", LocalizableField.Format.PLAIN_TEXT, schema, null));
            }
        };
        when(getter.getEnabledExtension(ResourceLocalizationProvider.class)).thenReturn(Mono.just(provider));
        when(getter.getEnabledExtensions(ResourceLocalizationDescriptor.class)).thenReturn(Flux.just(descriptor));
        when(provider.resolveAll(any(), any())).thenReturn(Flux.fromIterable(patches));
        return new DefaultResourceLocalizationService(getter, mapper);
    }
}
