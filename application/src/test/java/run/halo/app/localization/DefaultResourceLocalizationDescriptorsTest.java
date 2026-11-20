package run.halo.app.localization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import java.util.List;
import java.util.Locale;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.StaticMessageSource;
import reactor.core.publisher.Flux;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.content.Tag;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;
import run.halo.app.extension.Scheme;
import run.halo.app.extension.SchemeManager;
import run.halo.app.extension.event.SchemeAddedEvent;
import run.halo.app.plugin.PluginApplicationContext;
import run.halo.app.plugin.SpringPlugin;
import run.halo.app.plugin.SpringPluginManager;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;

class DefaultResourceLocalizationDescriptorsTest {
    private final ExtensionGetter getter = mock(ExtensionGetter.class);
    private final SchemeManager schemes = mock(SchemeManager.class);
    private final JsonMapper mapper = JsonMapper.builder().build();
    private final DefaultResourceLocalizationDescriptors registry =
            new DefaultResourceLocalizationDescriptors(getter, schemes, mapper);

    @Test
    void registeredAnnotatedExtensionUsesTheSharedDescriptorContract() {
        when(getter.getEnabledExtensions(ResourceLocalizationDescriptor.class)).thenReturn(Flux.empty());
        when(schemes.schemes()).thenReturn(List.of(Scheme.buildFromType(Tag.class)));
        var descriptors = registry.getDescriptors().collectList().block();
        assertThat(descriptors).hasSize(1);
        assertThat(descriptors.getFirst().titleField()).isEqualTo("displayName");
        assertThat(descriptors.getFirst().fields())
                .extracting(LocalizableField::key)
                .containsExactly("displayName", "description", "cover");
    }

    @Test
    void explicitDescriptorOwnsItsTypeAndDuplicatesRemainVisibleForRejection() {
        var explicit = new LocalizableAnnotations(mapper).descriptor(Tag.class);
        when(getter.getEnabledExtensions(ResourceLocalizationDescriptor.class))
                .thenReturn(Flux.just(explicit, explicit));
        when(schemes.schemes()).thenReturn(List.of(Scheme.buildFromType(Tag.class)));
        assertThat(registry.getDescriptors().collectList().block()).containsExactly(explicit, explicit);
    }

    @Test
    void annotationsAndOneExplicitDescriptorAreCombined() {
        when(getter.getEnabledExtensions(ResourceLocalizationDescriptor.class))
                .thenReturn(Flux.just(new PostLocalizationDescriptor()));
        when(schemes.schemes()).thenReturn(List.of(Scheme.buildFromType(Post.class)));

        var descriptor = registry.getDescriptors().blockFirst();

        assertThat(descriptor).isNotNull();
        assertThat(descriptor.fields())
                .extracting(LocalizableField::key)
                .containsExactly("title", "cover", "excerpt", "content.html", "content.raw");
        assertThat(descriptor.fieldCollections())
                .extracting(LocalizableFieldCollection::namespace)
                .containsExactly("htmlMetas");
        assertThat(descriptor.documents())
                .extracting(LocalizableDocument::htmlField)
                .containsExactly("content.html");
        assertThat(descriptor.displayName()).isEqualTo("core.post.title");
        assertThat(descriptor.titleField()).isEqualTo("title");
        assertThat(descriptor.summaryField()).isEqualTo("excerpt");
    }

    @Test
    void combinedLabelsKeepCoreAndPluginMessageOwners() {
        var manager = mock(SpringPluginManager.class);
        var beanFactory = new DefaultListableBeanFactory();
        beanFactory.setBeanClassLoader(PluginOwner.class.getClassLoader());
        try (var context = new PluginApplicationContext("owner", manager, beanFactory)) {
            var pluginMessages = new StaticMessageSource();
            pluginMessages.addMessage("plugin.localization.field.summary", Locale.ENGLISH, "Summary");
            pluginMessages.addMessage("Posts", Locale.ENGLISH, "Wrong resource label");
            pluginMessages.addMessage("Title", Locale.ENGLISH, "Wrong field label");
            context.registerBean(org.springframework.context.MessageSource.class, () -> pluginMessages);
            context.refresh();
            var wrapper = mock(PluginWrapper.class);
            var plugin = mock(Plugin.class, withSettings().extraInterfaces(SpringPlugin.class));
            when(wrapper.getPlugin()).thenReturn(plugin);
            when(((SpringPlugin) plugin).getApplicationContext()).thenReturn(context);
            when(manager.startedPlugins()).thenReturn(List.of(wrapper));

            var coreMessages = new StaticMessageSource();
            coreMessages.addMessage("core.post.title", Locale.ENGLISH, "Posts");
            coreMessages.addMessage("core.localization.field.title", Locale.ENGLISH, "Title");
            var localizedRegistry = new DefaultResourceLocalizationDescriptors(
                    getter, schemes, mapper, new DefaultResourceLocalizationDescriptorLocalizer(coreMessages, manager));
            when(getter.getEnabledExtensions(ResourceLocalizationDescriptor.class))
                    .thenReturn(Flux.just(summaryDescriptor()));
            when(schemes.schemes()).thenReturn(List.of(Scheme.buildFromType(Post.class)));

            var descriptor = localizedRegistry
                    .getDescriptors()
                    .contextWrite(reactorContext -> reactorContext.put(Locale.class, Locale.ENGLISH))
                    .blockFirst();

            assertThat(descriptor).isNotNull();
            assertThat(descriptor.displayName()).isEqualTo("Posts");
            assertThat(descriptor.fields())
                    .filteredOn(field -> field.key().equals("title"))
                    .singleElement()
                    .satisfies(field -> assertThat(field.schema().label()).isEqualTo("Title"));
            assertThat(descriptor.fields())
                    .filteredOn(field -> field.key().equals("summary"))
                    .singleElement()
                    .satisfies(field -> assertThat(field.schema().label()).isEqualTo("Summary"));
        }
    }

    @Test
    void conflictingSupplementDoesNotReplaceAnnotationDeclarations() {
        var supplemental = new ResourceLocalizationDescriptor() {
            @Override
            public run.halo.app.extension.GroupVersionKind supports() {
                return Post.GVK;
            }

            @Override
            public List<LocalizableField> fields() {
                return List.of(
                        LocalizableField.text("alternateTitle", "/spec/title").build());
            }
        };
        when(getter.getEnabledExtensions(ResourceLocalizationDescriptor.class)).thenReturn(Flux.just(supplemental));
        when(schemes.schemes()).thenReturn(List.of(Scheme.buildFromType(Post.class)));

        var descriptors = registry.getDescriptors().collectList().block();

        assertThat(descriptors).hasSize(2);
        assertThat(descriptors)
                .allSatisfy(descriptor -> assertThat(descriptor.supports()).isEqualTo(Post.GVK));
    }

    @Test
    void unregisteringATypeRemovesItFromTheNextSubscription() {
        when(getter.getEnabledExtensions(ResourceLocalizationDescriptor.class)).thenReturn(Flux.empty());
        when(schemes.schemes()).thenReturn(List.of(Scheme.buildFromType(Tag.class)), List.of());
        var result = registry.getDescriptors();
        assertThat(result.collectList().block()).hasSize(1);
        assertThat(result.collectList().block()).isEmpty();
    }

    @Test
    void unrelatedExtensionWithCustomSerializationIsNotAffected() {
        registry.onSchemeAdded(new SchemeAddedEvent(this, Scheme.buildFromType(Unrelated.class)));
        when(getter.getEnabledExtensions(ResourceLocalizationDescriptor.class)).thenReturn(Flux.empty());
        when(schemes.schemes()).thenReturn(List.of(Scheme.buildFromType(Unrelated.class)));
        assertThat(registry.getDescriptors().collectList().block()).isEmpty();
    }

    @Test
    void explicitUnannotatedSchemeDoesNotRequireATypeLevelGvkAnnotation() {
        var scheme = new Scheme(
                run.halo.app.extension.Unstructured.class,
                new run.halo.app.extension.GroupVersionKind("example.com", "v1", "Explicit"),
                "explicits",
                "explicit",
                mapper.createObjectNode());
        registry.onSchemeAdded(new SchemeAddedEvent(this, scheme));
        when(getter.getEnabledExtensions(ResourceLocalizationDescriptor.class)).thenReturn(Flux.empty());
        when(schemes.schemes()).thenReturn(List.of(scheme));
        assertThat(registry.getDescriptors().collectList().block()).isEmpty();
    }

    @Test
    void malformedAnnotationsFailAtTheSchemeRegistrationBoundary() {
        var event = new SchemeAddedEvent(this, Scheme.buildFromType(Invalid.class));
        assertThatThrownBy(() -> registry.onSchemeAdded(event)).isInstanceOf(IllegalArgumentException.class);
    }

    private static ResourceLocalizationDescriptor summaryDescriptor() {
        return new ResourceLocalizationDescriptor() {
            @Override
            public run.halo.app.extension.GroupVersionKind supports() {
                return Post.GVK;
            }

            @Override
            public List<LocalizableField> fields() {
                return List.of(LocalizableField.text("summary", "/spec/summary")
                        .label("plugin.localization.field.summary")
                        .build());
            }
        };
    }

    @JsonSerialize(using = ToStringSerializer.class)
    @GVK(group = "example.com", version = "v1", kind = "Unrelated", plural = "unrelateds", singular = "unrelated")
    static class Unrelated extends AbstractExtension {}

    static class PluginOwner {}

    @Data
    @GVK(group = "example.com", version = "v1", kind = "Invalid", plural = "invalids", singular = "invalid")
    static class Invalid extends AbstractExtension {
        @Localizable(key = "duplicate")
        private String first;

        @Localizable(key = "duplicate")
        private String second;
    }
}
