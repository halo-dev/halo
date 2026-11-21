package run.halo.app.localization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.json.JsonMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.extension.Metadata;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;

class CollectionLocalizationTest {
    private final PostLocalizationDescriptor descriptor = new PostLocalizationDescriptor();
    private final LocalizableFieldCollection collection =
            descriptor.fieldCollections().getFirst();
    private final LocalizationContext context =
            new LocalizationContext(Locale.CHINA, Locale.ENGLISH, List.of(), LocalizationScope.PUBLIC_PAGE);

    @Test
    void customNamesUseCurrentIdentityLocationsAndOnlyChangeDisplayValues() {
        var post = source(List.of(
                Map.of("property", "my-description", "content", "分享原文", "data-extra", "keep"),
                Map.of("name", "my-description", "content", "搜索原文")));
        var fields = List.of(
                field("name", "my-description", "搜索原文", "Search description"),
                field("property", "my-description", "分享原文", "Sharing description"));
        var service = service(post, fields);
        var translated = service.localize(post, context).block();
        assertThat(translated.getSpec().getHtmlMetas())
                .containsExactly(
                        Map.of("property", "my-description", "content", "Sharing description", "data-extra", "keep"),
                        Map.of("name", "my-description", "content", "Search description"));
        assertThat(post.getSpec().getHtmlMetas().getFirst().get("content")).isEqualTo("分享原文");
        var reordered = new ArrayList<>(post.getSpec().getHtmlMetas());
        reordered.add(reordered.removeFirst());
        post.getSpec().setHtmlMetas(reordered);
        assertThat(service.localize(post, context).block().getSpec().getHtmlMetas())
                .containsExactly(
                        translated.getSpec().getHtmlMetas().get(1),
                        translated.getSpec().getHtmlMetas().getFirst());
        post.getSpec().setTitle("Unrelated edit");
        assertThat(service.localize(post, context)
                        .block()
                        .getSpec()
                        .getHtmlMetas()
                        .getFirst()
                        .get("content"))
                .isEqualTo("Search description");
    }

    @ParameterizedTest
    @EnumSource(
            value = LocalizableSchema.Type.class,
            names = {"IMAGE", "LINK"})
    void referenceCollectionsReplaceOnlyDeclaredValuesAfterIdentityReordering(LocalizableSchema.Type type) {
        var schema = referenceSchema(type);
        var declared = LocalizableFieldCollection.builder("references", "/spec/htmlMetas")
                .valueField("payload")
                .entries(
                        "identity",
                        entries -> entries.add("description", schema).add("custom/value", schema))
                .build();
        var original = List.of(
                Map.of("identity", "custom/value", "payload", "/source-b", "extra", "keep"),
                Map.of("identity", "description", "payload", "/source-a"));
        var post = source(original);
        var fields = List.of(
                field(declared, "identity", "description", "/source-a", "/localized-a"),
                field(declared, "identity", "custom/value", "/source-b", "/localized-b"));
        var service = service(post, fields, descriptor(declared));
        var expected = List.of(
                Map.of("identity", "custom/value", "payload", "/localized-b", "extra", "keep"),
                Map.of("identity", "description", "payload", "/localized-a"));

        assertThat(service.localize(post, context).block().getSpec().getHtmlMetas())
                .containsExactlyElementsOf(expected);
        assertThat(post.getSpec().getHtmlMetas()).containsExactlyElementsOf(original);

        var reordered = List.of(original.get(1), original.getFirst());
        post.getSpec().setHtmlMetas(reordered);
        assertThat(service.localize(post, context).block().getSpec().getHtmlMetas())
                .containsExactly(expected.get(1), expected.getFirst());
        assertThat(post.getSpec().getHtmlMetas()).containsExactlyElementsOf(reordered);
    }

    @ParameterizedTest
    @EnumSource(
            value = LocalizableSchema.Type.class,
            names = {"IMAGE", "LINK"})
    void preservedCollectionReferencesCannotBeReplacedByApprovedPatches(LocalizableSchema.Type type) {
        var schema = referenceSchema(type);
        var declared = LocalizableFieldCollection.builder("references", "/spec/htmlMetas")
                .valueField("payload")
                .entries(
                        "identity",
                        entries -> entries.add("editable", schema)
                                .add("fixed", schema.toBuilder().policy(LocalizableSchema.Policy.PRESERVE)))
                .build();
        var original = List.of(
                Map.of("identity", "editable", "payload", "/original"),
                Map.of("identity", "fixed", "payload", "/fixed"));
        var post = source(original);
        var fixed = field(declared, "identity", "fixed", "/fixed", "/not-allowed");

        assertThat(service(post, List.of(fixed), descriptor(declared))
                        .localize(post, context)
                        .block())
                .isSameAs(post);
        var fields = List.of(fixed, field(declared, "identity", "editable", "/original", "/localized"));
        assertThat(service(post, fields, descriptor(declared))
                        .localize(post, context)
                        .block()
                        .getSpec()
                        .getHtmlMetas())
                .containsExactly(Map.of("identity", "editable", "payload", "/localized"), original.get(1));
        assertThat(post.getSpec().getHtmlMetas()).containsExactlyElementsOf(original);
    }

    @Test
    void removedRenamedDuplicateStaleAndDraftFieldsRetainSourceValues() {
        var post = source(List.of(
                Map.of("name", "renamed", "content", "A"),
                Map.of("name", "duplicate", "content", "B"),
                Map.of("name", "duplicate", "content", "B"),
                Map.of("name", "stale", "content", "C"),
                Map.of("name", "draft", "content", "D")));
        var fields = List.of(
                field("name", "old-name", "A", "wrong"),
                field("name", "duplicate", "B", "wrong"),
                field("name", "stale", "old", "wrong"),
                field("name", "removed", "deleted", "wrong"),
                new LocalizationPatch.Field(
                        collection.fieldKey("name", "draft"),
                        LocalizationHash.of("D"),
                        "wrong",
                        LocalizationPatch.State.DRAFT),
                new LocalizationPatch.Field(
                        "/spec/title", LocalizationHash.of("title"), "wrong", LocalizationPatch.State.APPROVED));
        assertThat(service(post, fields).localize(post, context).block()).isSameAs(post);
    }

    @Test
    void singlePagesDeclareTheSameCollectionWithoutFixedMetadataNames() {
        var page = new SinglePage();
        var spec = new SinglePage.SinglePageSpec();
        spec.setHtmlMetas(List.of(Map.of("name", "custom", "content", "page")));
        page.setSpec(spec);
        var fields = new SinglePageLocalizationDescriptor()
                .resolveDisplayFields(JsonMapper.builder().build().valueToTree(page));
        assertThat(fields).extracting(LocalizableField::key).contains(collection.fieldKey("name", "custom"));
        assertThat(descriptor.fields()).extracting(LocalizableField::key).noneMatch(key -> key.startsWith("seo."));
    }

    private DefaultResourceLocalizationService service(Post source, List<LocalizationPatch.Field> fields) {
        return service(source, fields, descriptor);
    }

    private DefaultResourceLocalizationService service(
            Post source, List<LocalizationPatch.Field> fields, ResourceLocalizationDescriptor resourceDescriptor) {
        var getter = mock(ExtensionGetter.class);
        var provider = mock(ResourceLocalizationProvider.class);
        when(getter.getEnabledExtension(ResourceLocalizationProvider.class)).thenReturn(Mono.just(provider));
        when(getter.getEnabledExtensions(ResourceLocalizationDescriptor.class))
                .thenReturn(Flux.just(resourceDescriptor));
        when(provider.resolveAll(any(), any()))
                .thenReturn(Flux.just(new LocalizationPatch(ResourceReference.from(source), Locale.ENGLISH, fields)));
        return new DefaultResourceLocalizationService(
                getter, JsonMapper.builder().build());
    }

    private LocalizationPatch.Field field(String identity, String name, String source, String value) {
        return field(collection, identity, name, source, value);
    }

    private LocalizationPatch.Field field(
            LocalizableFieldCollection declared, String identity, String name, String source, String value) {
        return new LocalizationPatch.Field(
                declared.fieldKey(identity, name),
                LocalizationHash.of(source),
                value,
                LocalizationPatch.State.APPROVED);
    }

    private static LocalizableSchema referenceSchema(LocalizableSchema.Type type) {
        return (type == LocalizableSchema.Type.IMAGE ? LocalizableSchema.image() : LocalizableSchema.link()).build();
    }

    private static ResourceLocalizationDescriptor descriptor(LocalizableFieldCollection declared) {
        return new PostLocalizationDescriptor() {
            @Override
            public List<LocalizableFieldCollection> fieldCollections() {
                return List.of(declared);
            }
        };
    }

    private static Post source(List<Map<String, String>> metas) {
        var post = new Post();
        var metadata = new Metadata();
        metadata.setName("post");
        post.setMetadata(metadata);
        var spec = new Post.PostSpec();
        spec.setTitle("title");
        spec.setHtmlMetas(metas);
        post.setSpec(spec);
        return post;
    }
}
