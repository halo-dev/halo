package run.halo.app.localization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.util.List;
import lombok.Data;
import org.junit.jupiter.api.Test;
import run.halo.app.core.extension.Menu;
import run.halo.app.core.extension.MenuItem;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.core.extension.content.Category;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.content.Reply;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.core.extension.content.Tag;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.Extension;
import run.halo.app.extension.GVK;

class LocalizableAnnotationsTest {
    private final JsonMapper mapper = JsonMapper.builder().build();
    private final LocalizableAnnotations annotations = new LocalizableAnnotations(mapper);

    @Test
    void builtInExtensionsUseAnnotationDescriptors() {
        assertDescriptor(
                Tag.class,
                "core.post_tag.title",
                "displayName",
                LocalizableField.text("displayName", "/spec/displayName")
                        .label("core.localization.field.displayName")
                        .required(true),
                LocalizableField.text("description", "/spec/description")
                        .label("core.localization.field.description")
                        .multiline(true),
                LocalizableField.image("cover", "/spec/cover").label("core.localization.field.cover"));
        assertDescriptor(
                Category.class,
                "core.post_category.title",
                "displayName",
                LocalizableField.text("displayName", "/spec/displayName")
                        .label("core.localization.field.displayName")
                        .required(true),
                LocalizableField.text("description", "/spec/description")
                        .label("core.localization.field.description")
                        .multiline(true),
                LocalizableField.image("cover", "/spec/cover").label("core.localization.field.cover"));
        assertDescriptor(
                Attachment.class,
                "core.attachment.title",
                "displayName",
                LocalizableField.text("displayName", "/spec/displayName")
                        .label("core.localization.field.displayName")
                        .required(true));
        assertDescriptor(
                Menu.class,
                "core.menu.title",
                "displayName",
                LocalizableField.text("displayName", "/spec/displayName")
                        .label("core.localization.field.displayName")
                        .required(true));
        assertDescriptor(
                MenuItem.class,
                "core.menu_item.title",
                "displayName",
                LocalizableField.text("displayName", "/status/displayName")
                        .label("core.localization.field.displayName"));
        assertDescriptor(
                Post.class,
                "core.post.title",
                "title",
                "excerpt",
                LocalizableField.text("title", "/spec/title")
                        .label("core.localization.field.title")
                        .required(true),
                LocalizableField.image("cover", "/spec/cover").label("core.localization.field.cover"),
                LocalizableField.text("excerpt", "/status/excerpt")
                        .label("core.localization.field.excerpt")
                        .multiline(true));
        assertDescriptor(
                SinglePage.class,
                "core.page.title",
                "title",
                "excerpt",
                LocalizableField.text("title", "/spec/title")
                        .label("core.localization.field.title")
                        .required(true),
                LocalizableField.image("cover", "/spec/cover").label("core.localization.field.cover"),
                LocalizableField.text("excerpt", "/status/excerpt")
                        .label("core.localization.field.excerpt")
                        .multiline(true));
        assertDescriptor(
                User.class,
                "core.author.title",
                "displayName",
                LocalizableField.text("displayName", "/spec/displayName")
                        .label("core.localization.field.displayName")
                        .required(true),
                LocalizableField.text("bio", "/spec/bio")
                        .label("core.localization.field.bio")
                        .multiline(true));
        assertDescriptor(
                Comment.class,
                "core.comment.title",
                null,
                LocalizableField.html("content.html", "/spec/content").label("core.localization.field.body"));
        assertDescriptor(
                Reply.class,
                "core.reply.title",
                null,
                LocalizableField.html("content.html", "/spec/content").label("core.localization.field.body"));
        assertThat(annotations.fields(Tag.class)).isSameAs(annotations.fields(Tag.class));
    }

    private void assertDescriptor(
            Class<? extends Extension> type, String label, String title, LocalizableField.Builder... fields) {
        assertDescriptor(type, label, title, null, fields);
    }

    private void assertDescriptor(
            Class<? extends Extension> type,
            String label,
            String title,
            String summary,
            LocalizableField.Builder... fields) {
        var descriptor = annotations.descriptor(type);
        assertThat(descriptor.displayName()).isEqualTo(label);
        assertThat(descriptor.titleField()).isEqualTo(title);
        assertThat(descriptor.summaryField()).isEqualTo(summary);
        assertThat(descriptor.fields()).isEqualTo(LocalizableField.list(fields));
    }

    @Test
    void recordsUseSerializedNamesAndExplicitStableKeys() {
        var fields = annotations.fields(Product.class);
        assertThat(annotations.identityPointer(Product.class)).isEqualTo("/product_id");
        assertThat(fields).extracting(LocalizableField::key).containsExactly("heading", "artwork", "destination");
        assertThat(fields).extracting(LocalizableField::pointer).containsExactly("/display_title", "/visual", "/link");
        assertThat(fields)
                .extracting(field -> field.schema().type())
                .containsExactly(
                        LocalizableSchema.Type.TEXT, LocalizableSchema.Type.IMAGE, LocalizableSchema.Type.LINK);
    }

    @Test
    void annotatedGettersAndInheritedFieldsAreSupported() {
        var fields = annotations.fields(GetterResource.class);
        assertThat(fields).extracting(LocalizableField::pointer).containsExactly("/inherited", "/renamed");
        assertThat(fields).extracting(LocalizableField::key).containsExactly("base", "getter");
    }

    @Test
    void annotationsDeclaredOnInterfaceAccessorsAreSupported() {
        var fields = annotations.fields(InterfaceResource.class);
        assertThat(fields).extracting(LocalizableField::pointer).containsExactly("/display_title");
        assertThat(fields).extracting(LocalizableField::key).containsExactly("heading");
    }

    @Test
    void nestedObjectsCreateIndependentUnitsAndEscapeJsonPointers() {
        var field = annotations.fields(Nested.class).getFirst();
        assertThat(field.key()).isEqualTo("heading");
        assertThat(field.pointer()).isEqualTo("/details/title~1~0");
    }

    @Test
    void arraySchemaPreservesNumbersAndUnknownMembersAndSupportsTextTranslation() throws Exception {
        var schema = annotations.fields(Gallery.class).getFirst().schema();
        assertThat(schema.type()).isEqualTo(LocalizableSchema.Type.ARRAY);
        assertThat(schema.identityField()).isEqualTo("id");
        assertThat(schema.independentStructure()).isTrue();
        assertThat(schema.items().properties().get("title").supportsGeneration())
                .isTrue();
        assertThat(schema.items().properties().get("image").supportsGeneration())
                .isFalse();
        var source = mapper.readTree("""
            [{"id":"a","title":"Source","image":"/one.png","price":12,"private":"keep"}]
            """);
        var translated = mapper.readTree("""
            [{"id":"a","title":"Translated","image":"/two.png","price":999,"private":"replace"}]
            """);
        assertThatThrownBy(() -> LocalizableValues.project(schema, source, translated))
                .isInstanceOf(IllegalArgumentException.class);
        var permitted = mapper.readTree("""
            [{"id":"a","title":"Translated","image":"/two.png"}]
            """);
        var projected = LocalizableValues.project(schema, source, permitted);
        assertThat(projected.at("/0/title").asText()).isEqualTo("Translated");
        assertThat(projected.at("/0/image").asText()).isEqualTo("/two.png");
        assertThat(projected.at("/0/price").asInt()).isEqualTo(12);
        assertThat(projected.at("/0/private").asText()).isEqualTo("keep");
    }

    @Test
    void scalarArraysAndNestedArraysUseDeclaredItemSemantics() {
        var fields = annotations.fields(Labels.class);
        assertThat(fields.getFirst().schema().items().type()).isEqualTo(LocalizableSchema.Type.TEXT);
        assertThat(fields.get(1).schema().items().policy()).isEqualTo(LocalizableSchema.Policy.PRESERVE);
        var nested = annotations.fields(NestedGallery.class).getFirst().schema();
        assertThat(nested.items().properties().get("labels").items().type()).isEqualTo(LocalizableSchema.Type.TEXT);
    }

    @Test
    void ignoredAnnotatedFieldsAreRejected() {
        assertThatThrownBy(() -> annotations.fields(Ignored.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ignored");
    }

    @Test
    void ignoredGetterCannotHideAnnotatedField() {
        assertThatThrownBy(() -> annotations.fields(IgnoredGetter.class)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void writeOnlyFieldsAreRejected() {
        assertThatThrownBy(() -> annotations.fields(WriteOnly.class)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void readOnlyAndClassIgnoredFieldsAreRejected() {
        assertThatThrownBy(() -> annotations.fields(ReadOnly.class)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> annotations.fields(ClassIgnored.class)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void unwrappedObjectsRequireExplicitDescriptors() {
        assertThatThrownBy(() -> annotations.fields(Unwrapped.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unwrapped");
    }

    @Test
    void conflictingAccessorDeclarationsAreRejected() {
        assertThatThrownBy(() -> annotations.fields(Conflicting.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Conflicting");
    }

    @Test
    void duplicateKeysAcrossNestedObjectsAreRejected() {
        assertThatThrownBy(() -> annotations.fields(Duplicates.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Duplicate");
    }

    @Test
    void recursiveAndUntypedDeclarationsAreRejected() {
        assertThatThrownBy(() -> annotations.fields(Recursive.class)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> annotations.fields(Untyped.class)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void identityCannotBeTranslatedOrMissing() {
        assertThatThrownBy(() -> annotations.identityPointer(TranslatedId.class))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> annotations.identityPointer(Title.class)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void identitiesRejectFractionalOrAmbiguousNumbersAtDeclarationTime() {
        assertThatThrownBy(() -> annotations.identityPointer(FractionalId.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("integers");
        assertThat(annotations.identityPointer(IntegerId.class)).isEqualTo("/id");
    }

    @Test
    void extensionRootFieldsAreSupported() {
        var descriptor = annotations.descriptor(RootFieldExtension.class);
        assertThat(descriptor.fields()).extracting(LocalizableField::pointer).containsExactly("/outside");
    }

    record Product(
            @JsonProperty("product_id") @LocalizationId String id,

            @JsonProperty("display_title") @Localizable(key = "heading")
            String title,

            @Localizable(key = "artwork", type = LocalizableSchema.Type.IMAGE)
            String visual,

            @Localizable(key = "destination", type = LocalizableSchema.Type.LINK)
            String link,

            String privateValue) {}

    static class Base {
        @Localizable(key = "base")
        public String inherited;
    }

    static class GetterResource extends Base {
        private String title;

        @JsonProperty("renamed")
        @Localizable(key = "getter")
        public String getTitle() {
            return title;
        }

        public void setTitle(String value) {
            title = value;
        }
    }

    interface InterfaceFields {
        @JsonProperty("display_title")
        @Localizable(key = "heading")
        String getTitle();

        @JsonProperty("display_title")
        void setTitle(String value);
    }

    static class InterfaceResource implements InterfaceFields {
        private String title;

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public void setTitle(String value) {
            title = value;
        }
    }

    record Nested(@LocalizableNested Title details) {}

    record Title(
            @JsonProperty("title/~") @Localizable(key = "heading")
            String title) {}

    record Gallery(
            @Localizable(
                    key = "gallery",
                    type = LocalizableSchema.Type.ARRAY,
                    identityField = "id",
                    independentStructure = true)
            List<Slide> slides) {}

    record Slide(
            @Localizable(policy = Localizable.Operation.PRESERVE)
            String id,

            @Localizable String title,

            @Localizable(type = LocalizableSchema.Type.IMAGE)
            String image,

            @Localizable(type = LocalizableSchema.Type.NUMBER)
            int price,

            String privateValue) {}

    record Labels(
            @Localizable(key = "labels", type = LocalizableSchema.Type.ARRAY, itemType = LocalizableSchema.Type.TEXT)
            List<String> labels,

            @Localizable(
                    key = "ids",
                    type = LocalizableSchema.Type.ARRAY,
                    itemType = LocalizableSchema.Type.TEXT,
                    itemPolicy = Localizable.Operation.PRESERVE)
            List<String> ids) {}

    record NestedGallery(
            @Localizable(key = "gallery", type = LocalizableSchema.Type.ARRAY)
            List<Labels> groups) {}

    record Ignored(@JsonIgnore @Localizable(key = "hidden") String hidden) {}

    record WriteOnly(
            @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) @Localizable(key = "hidden")
            String hidden) {}

    record ReadOnly(
            @JsonProperty(access = JsonProperty.Access.READ_ONLY) @Localizable(key = "title")
            String title) {}

    @JsonIgnoreProperties("title")
    record ClassIgnored(@Localizable(key = "title") String title) {}

    record Unwrapped(@JsonUnwrapped @LocalizableNested Title title) {}

    record Duplicates(
            @LocalizableNested Title first,
            @LocalizableNested Title second) {}

    record Recursive(@LocalizableNested Recursive child) {}

    record Untyped(
            @Localizable(key = "array", type = LocalizableSchema.Type.ARRAY)
            List<Object> values) {}

    record FractionalId(@LocalizationId double id) {}

    record IntegerId(@LocalizationId java.math.BigInteger id) {}

    record TranslatedId(
            @LocalizationId @Localizable(key = "id") String id) {}

    static class Conflicting {
        @Localizable(key = "field")
        private String title;

        @Localizable(key = "getter")
        public String getTitle() {
            return title;
        }

        public void setTitle(String value) {
            title = value;
        }
    }

    static class IgnoredGetter {
        @Localizable(key = "hidden")
        private String title;

        @JsonIgnore
        public String getTitle() {
            return title;
        }

        public void setTitle(String value) {
            title = value;
        }
    }

    @Data
    @GVK(group = "example.com", version = "v1", kind = "RootField", plural = "root-fields", singular = "root-field")
    static class RootFieldExtension extends AbstractExtension {
        @Localizable(key = "outside")
        private String outside;
    }
}
