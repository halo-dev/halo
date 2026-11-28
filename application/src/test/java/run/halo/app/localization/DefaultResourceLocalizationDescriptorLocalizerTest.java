package run.halo.app.localization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.support.StaticMessageSource;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.plugin.PluginApplicationContext;
import run.halo.app.plugin.SpringPlugin;
import run.halo.app.plugin.SpringPluginManager;

class DefaultResourceLocalizationDescriptorLocalizerTest {

    @Test
    void resolvesCoreLabelsAndFallsBackToTheDeclaredLiteral() {
        var coreMessages = new StaticMessageSource();
        coreMessages.addMessage("core.post.title", Locale.ENGLISH, "Posts");
        coreMessages.addMessage("core.localization.field.title", Locale.ENGLISH, "Title");
        var localizer =
                new DefaultResourceLocalizationDescriptorLocalizer(coreMessages, mock(SpringPluginManager.class));

        var localized = localizer.localize(descriptor(), Post.class, Locale.ENGLISH);

        assertThat(localized.displayName()).isEqualTo("Posts");
        assertThat(localized.fields()).singleElement().satisfies(field -> {
            assertThat(field.schema().label()).isEqualTo("Title");
            assertThat(field.group()).isEqualTo("Literal group");
        });
    }

    @Test
    void shipsEnglishAndChineseCoreTranslations() throws IOException {
        assertThat(properties("messages.properties").getProperty("core.post.title"))
                .isEqualTo("Posts");
        assertThat(properties("messages_zh.properties").getProperty("core.post.title"))
                .isEqualTo("文章");
        assertThat(properties("messages_es.properties").getProperty("core.post.title"))
                .isEqualTo("Publicaciones");
    }

    @Test
    void resolvesNestedSchemaLabelsWithoutChangingTheDeclarations() {
        var coreMessages = new StaticMessageSource();
        coreMessages.addMessage("core.post.title", Locale.ENGLISH, "Posts");
        coreMessages.addMessage("core.localization.field.title", Locale.ENGLISH, "Title");
        coreMessages.addMessage("core.localization.field.cover", Locale.ENGLISH, "Cover");
        coreMessages.addMessage("core.localization.field.description", Locale.ENGLISH, "Description");
        coreMessages.addMessage("core.localization.group.seo", Locale.ENGLISH, "Search and sharing");
        var localizer =
                new DefaultResourceLocalizationDescriptorLocalizer(coreMessages, mock(SpringPluginManager.class));
        var item = LocalizableSchema.object()
                .label("core.localization.field.description")
                .property("title", LocalizableSchema.text().label("core.localization.field.title"))
                .property("cover", LocalizableSchema.image().label("core.localization.field.cover"))
                .build();
        var schema = LocalizableSchema.array(item)
                .label("core.localization.group.seo")
                .build();
        var descriptor = new ResourceLocalizationDescriptor() {
            @Override
            public GroupVersionKind supports() {
                return Post.GVK;
            }

            @Override
            public List<LocalizableField> fields() {
                return List.of(new LocalizableField(
                        "sections",
                        "/spec/sections",
                        LocalizableField.Format.PLAIN_TEXT,
                        schema,
                        "core.localization.group.seo"));
            }

            @Override
            public String displayName() {
                return "core.post.title";
            }
        };

        var field = localizer
                .localize(descriptor, Post.class, Locale.ENGLISH)
                .fields()
                .getFirst();

        assertThat(field.group()).isEqualTo("Search and sharing");
        assertThat(field.schema().label()).isEqualTo("Search and sharing");
        assertThat(field.schema().items().label()).isEqualTo("Description");
        assertThat(field.schema().items().properties().get("title").label()).isEqualTo("Title");
        assertThat(field.schema().items().properties().get("cover").label()).isEqualTo("Cover");
        assertThat(schema.label()).isEqualTo("core.localization.group.seo");
        assertThat(item.properties().get("title").label()).isEqualTo("core.localization.field.title");
    }

    @Test
    void resolvesPluginLabelsThenCoreLabelsThenTheDeclaredLiteral() {
        var manager = mock(SpringPluginManager.class);
        var beanFactory = new DefaultListableBeanFactory();
        beanFactory.setBeanClassLoader(PluginOwner.class.getClassLoader());
        try (var context = new PluginApplicationContext("owner", manager, beanFactory)) {
            var pluginMessages = new StaticMessageSource();
            pluginMessages.addMessage("plugin.resource.notice", Locale.ENGLISH, "Plugin notices");
            context.registerBean(MessageSource.class, () -> pluginMessages);
            context.refresh();
            var pluginWrapper = wrapper(context);
            when(manager.startedPlugins()).thenReturn(List.of(pluginWrapper));

            var coreMessages = new StaticMessageSource();
            coreMessages.addMessage("core.localization.field.title", Locale.ENGLISH, "Title");
            var localizer = new DefaultResourceLocalizationDescriptorLocalizer(coreMessages, manager);

            var localized = localizer.localize(pluginDescriptor(), PluginOwner.class, Locale.ENGLISH);

            assertThat(localized.displayName()).isEqualTo("Plugin notices");
            assertThat(localized.fields()).singleElement().satisfies(field -> {
                assertThat(field.schema().label()).isEqualTo("Title");
                assertThat(field.group()).isEqualTo("Literal group");
            });
        }
    }

    private static PluginWrapper wrapper(PluginApplicationContext context) {
        var wrapper = mock(PluginWrapper.class);
        var plugin = mock(Plugin.class, withSettings().extraInterfaces(SpringPlugin.class));
        when(wrapper.getPlugin()).thenReturn(plugin);
        when(((SpringPlugin) plugin).getApplicationContext()).thenReturn(context);
        return wrapper;
    }

    private static Properties properties(String name) throws IOException {
        var values = new Properties();
        try (var reader =
                Files.newBufferedReader(Path.of("src/main/resources/config/i18n", name), StandardCharsets.UTF_8)) {
            values.load(reader);
        }
        return values;
    }

    private static ResourceLocalizationDescriptor descriptor() {
        return descriptor("core.post.title");
    }

    private static ResourceLocalizationDescriptor pluginDescriptor() {
        return descriptor("plugin.resource.notice");
    }

    private static ResourceLocalizationDescriptor descriptor(String displayName) {
        return new ResourceLocalizationDescriptor() {
            @Override
            public GroupVersionKind supports() {
                return Post.GVK;
            }

            @Override
            public List<LocalizableField> fields() {
                var schema = LocalizableSchema.text()
                        .label("core.localization.field.title")
                        .build();
                return List.of(new LocalizableField(
                        "title", "/spec/title", LocalizableField.Format.PLAIN_TEXT, schema, "Literal group"));
            }

            @Override
            public String displayName() {
                return displayName;
            }
        };
    }

    static final class PluginOwner {}
}
