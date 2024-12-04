package run.halo.app.plugin.extensionpoint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static run.halo.app.infra.SystemSetting.ExtensionPointEnabled.GROUP;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pf4j.ExtensionPoint;
import org.pf4j.PluginManager;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.annotation.Order;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.extension.Metadata;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.SystemSetting.ExtensionPointEnabled;
import run.halo.app.plugin.extensionpoint.ExtensionPointDefinition.ExtensionPointType;

@ExtendWith(MockitoExtension.class)
class DefaultExtensionGetterTest {

    @Mock
    ExtensionPointDefinitionGetter extensionPointDefinitionGetter;

    @Mock
    ExtensionDefinitionGetter extensionDefinitionGetter;

    @Mock
    PluginManager pluginManager;

    @Mock
    SystemConfigurableEnvironmentFetcher configFetcher;

    @Mock
    BeanFactory beanFactory;

    @Mock
    ObjectProvider<FakeExtensionPoint> extensionPointObjectProvider;

    @InjectMocks
    DefaultExtensionGetter getter;

    @Test
    void shouldGetExtensionBySingletonDefinitionWhenExtensionPointEnabledSet() {
        // prepare extension point definition
        when(extensionPointDefinitionGetter.getByClassName(any()))
            .thenReturn(Mono.fromSupplier(
                () -> createExtensionPointDefinition("fake-extension-point",
                    FakeExtensionPoint.class,
                    ExtensionPointType.SINGLETON))
            );

        when(extensionDefinitionGetter.get(eq("fake-extension")))
            .thenReturn(Mono.fromSupplier(() -> createExtensionDefinition(
                "fake-extension",
                FakeExtensionPointImpl.class,
                "fake-extension-point")));

        when(configFetcher.fetch(GROUP, ExtensionPointEnabled.class))
            .thenReturn(Mono.fromSupplier(() -> {
                var extensionPointEnabled = new ExtensionPointEnabled();
                extensionPointEnabled.put("fake-extension-point",
                    new LinkedHashSet<>(List.of("fake-extension")));
                return extensionPointEnabled;
            }));

        @SuppressWarnings("unchecked")
        ObjectProvider<FakeExtensionPoint> objectProvider = mock(ObjectProvider.class);
        when(objectProvider.orderedStream())
            .thenReturn(Stream.of(new FakeExtensionPointDefaultImpl()));
        when(beanFactory.getBeanProvider(FakeExtensionPoint.class)).thenReturn(objectProvider);

        var extensionImpl = new FakeExtensionPointImpl();

        var spyGetter = spy(getter);
        doReturn(List.of(extensionImpl)).when(spyGetter)
            .lookExtensions(eq(FakeExtensionPoint.class));

        spyGetter.getEnabledExtensions(FakeExtensionPoint.class)
            .as(StepVerifier::create)
            .expectNext(extensionImpl)
            .verifyComplete();
    }

    @Test
    void shouldGetDefaultSingletonDefinitionWhileExtensionPointEnabledNotSet() {
        when(extensionPointDefinitionGetter.getByClassName(any()))
            .thenReturn(Mono.fromSupplier(
                () -> createExtensionPointDefinition("fake-extension-point",
                    FakeExtensionPoint.class,
                    ExtensionPointType.SINGLETON))
            );

        when(configFetcher.fetch(GROUP, ExtensionPointEnabled.class))
            .thenReturn(Mono.empty());

        @SuppressWarnings("unchecked")
        ObjectProvider<FakeExtensionPoint> objectProvider = mock(ObjectProvider.class);
        var extensionDefaultImpl = new FakeExtensionPointDefaultImpl();
        when(objectProvider.orderedStream())
            .thenReturn(Stream.of(extensionDefaultImpl));
        when(beanFactory.getBeanProvider(FakeExtensionPoint.class)).thenReturn(objectProvider);

        var spyGetter = spy(getter);
        doReturn(List.of()).when(spyGetter)
            .lookExtensions(eq(FakeExtensionPoint.class));

        spyGetter.getEnabledExtensions(FakeExtensionPoint.class)
            .as(StepVerifier::create)
            .expectNext(extensionDefaultImpl)
            .verifyComplete();
    }

    @Test
    void shouldGetMultiInstanceExtensionWhileExtensionPointEnabledSet() {
        // prepare extension point definition
        when(extensionPointDefinitionGetter.getByClassName(any()))
            .thenReturn(Mono.fromSupplier(
                () -> createExtensionPointDefinition("fake-extension-point",
                    FakeExtensionPoint.class,
                    ExtensionPointType.MULTI_INSTANCE))
            );

        when(extensionDefinitionGetter.get(eq("fake-extension")))
            .thenReturn(Mono.fromSupplier(() -> createExtensionDefinition(
                "fake-extension",
                FakeExtensionPointImpl.class,
                "fake-extension-point")));

        when(extensionDefinitionGetter.get(eq("default-fake-extension")))
            .thenReturn(Mono.fromSupplier(() -> createExtensionDefinition(
                "default-fake-extension",
                FakeExtensionPointDefaultImpl.class,
                "fake-extension-point")));

        when(configFetcher.fetch(GROUP, ExtensionPointEnabled.class))
            .thenReturn(Mono.fromSupplier(() -> {
                var extensionPointEnabled = new ExtensionPointEnabled();
                extensionPointEnabled.put("fake-extension-point",
                    new LinkedHashSet<>(List.of("default-fake-extension", "fake-extension")));
                return extensionPointEnabled;
            }));

        @SuppressWarnings("unchecked")
        ObjectProvider<FakeExtensionPoint> objectProvider = mock(ObjectProvider.class);
        var extensionDefaultImpl = new FakeExtensionPointDefaultImpl();
        when(objectProvider.orderedStream())
            .thenReturn(Stream.of(extensionDefaultImpl));
        when(beanFactory.getBeanProvider(FakeExtensionPoint.class)).thenReturn(objectProvider);

        var extensionImpl = new FakeExtensionPointImpl();
        var anotherExtensionImpl = new FakeExtensionPoint() {
        };

        var spyGetter = spy(getter);
        doReturn(List.of(extensionImpl, anotherExtensionImpl)).when(spyGetter)
            .lookExtensions(eq(FakeExtensionPoint.class));

        spyGetter.getEnabledExtensions(FakeExtensionPoint.class)
            .as(StepVerifier::create)
            // should keep the order of enabled extensions
            .expectNext(extensionDefaultImpl)
            .expectNext(extensionImpl)
            .verifyComplete();
    }


    @Test
    void shouldGetMultiInstanceExtensionWhileExtensionPointEnabledNotSet() {
        // prepare extension point definition
        when(extensionPointDefinitionGetter.getByClassName(any()))
            .thenReturn(Mono.fromSupplier(
                () -> createExtensionPointDefinition("fake-extension-point",
                    FakeExtensionPoint.class,
                    ExtensionPointType.MULTI_INSTANCE))
            );

        when(configFetcher.fetch(GROUP, ExtensionPointEnabled.class))
            .thenReturn(Mono.empty());

        @SuppressWarnings("unchecked")
        ObjectProvider<FakeExtensionPoint> objectProvider = mock(ObjectProvider.class);
        var extensionDefaultImpl = new FakeExtensionPointDefaultImpl();
        when(objectProvider.orderedStream())
            .thenReturn(Stream.of(extensionDefaultImpl));
        when(beanFactory.getBeanProvider(FakeExtensionPoint.class)).thenReturn(objectProvider);

        var extensionImpl = new FakeExtensionPointImpl();
        var anotherExtensionImpl = new FakeExtensionPoint() {
        };

        var spyGetter = spy(getter);
        doReturn(List.of(extensionImpl, anotherExtensionImpl)).when(spyGetter)
            .lookExtensions(eq(FakeExtensionPoint.class));

        spyGetter.getEnabledExtensions(FakeExtensionPoint.class)
            .as(StepVerifier::create)
            // should keep the order according to @Order annotation
            // order is 1
            .expectNext(extensionImpl)
            // order is 2
            .expectNext(extensionDefaultImpl)
            // order is not set
            .expectNext(anotherExtensionImpl)
            .verifyComplete();
    }

    @Test
    void shouldGetExtensionsFromPluginManagerAndApplicationContext() {
        var extensionFromPlugin = new FakeExtensionPointDefaultImpl();
        var extensionFromAppContext = new FakeExtensionPointImpl();

        var spyGetter = spy(getter);
        doReturn(List.of(extensionFromPlugin)).when(spyGetter)
            .lookExtensions(eq(FakeExtensionPoint.class));

        when(beanFactory.getBeanProvider(FakeExtensionPoint.class))
            .thenReturn(extensionPointObjectProvider);
        when(extensionPointObjectProvider.orderedStream())
            .thenReturn(Stream.of(extensionFromAppContext));
        var extensions = spyGetter.getExtensionList(FakeExtensionPoint.class);
        assertEquals(List.of(extensionFromAppContext, extensionFromPlugin), extensions);
    }

    interface FakeExtensionPoint extends ExtensionPoint {

    }

    @Order(1)
    static class FakeExtensionPointImpl implements FakeExtensionPoint {
    }

    @Order(2)
    static class FakeExtensionPointDefaultImpl implements FakeExtensionPoint {
    }

    ExtensionDefinition createExtensionDefinition(String name, Class<?> clazz, String epdName) {
        var ed = new ExtensionDefinition();
        var metadata = new Metadata();
        metadata.setName(name);
        ed.setMetadata(metadata);
        var spec = new ExtensionDefinition.ExtensionSpec();
        spec.setClassName(clazz.getName());
        spec.setExtensionPointName(epdName);
        ed.setSpec(spec);
        return ed;
    }

    ExtensionPointDefinition createExtensionPointDefinition(String name,
        Class<?> clazz,
        ExtensionPointType type) {
        var epd = new ExtensionPointDefinition();
        var metadata = new Metadata();
        metadata.setName(name);
        epd.setMetadata(metadata);
        var spec = new ExtensionPointDefinition.ExtensionPointSpec();
        spec.setClassName(clazz.getName());
        spec.setType(type);
        epd.setSpec(spec);
        return epd;
    }

}