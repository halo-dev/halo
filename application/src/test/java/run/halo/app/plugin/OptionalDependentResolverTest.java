package run.halo.app.plugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.pf4j.PluginDependency;
import org.pf4j.PluginDescriptor;

/**
 * Tests for {@link OptionalDependentResolver}.
 *
 * @author guqing
 * @since 2.20.11
 */
class OptionalDependentResolverTest {

    @Test
    void testNoPlugins() {
        OptionalDependentResolver resolver = new OptionalDependentResolver(List.of());
        assertTrue(resolver.getOptionalDependents("nonexistent").isEmpty(),
            "No dependents expected for non-existent plugin");
    }

    @Test
    void testSinglePluginNoDependencies() {
        var pluginA = createpluginDescriptor("A", List.of());
        OptionalDependentResolver resolver = new OptionalDependentResolver(List.of(pluginA));

        assertTrue(resolver.getOptionalDependents("A").isEmpty(), "A has no dependents");
    }

    @Test
    void testSingleOptionalDependency() {
        var pluginA = createpluginDescriptor("A", List.of(new PluginDependency("B?")));
        var pluginB = createpluginDescriptor("B", List.of());
        OptionalDependentResolver resolver =
            new OptionalDependentResolver(List.of(pluginA, pluginB));

        assertEquals(List.of("A"), resolver.getOptionalDependents("B"),
            "B should have A as dependent");
        assertTrue(resolver.getOptionalDependents("A").isEmpty(), "A has no dependents");
    }

    @Test
    void testMultipleOptionalDependencies() {
        var pluginA = createpluginDescriptor("A", List.of(
            new PluginDependency("B?"),
            new PluginDependency("C?"))
        );

        var pluginB = createpluginDescriptor("B", List.of());

        var pluginC = createpluginDescriptor("C", List.of());

        OptionalDependentResolver resolver =
            new OptionalDependentResolver(List.of(pluginA, pluginB, pluginC));

        assertEquals(List.of("A"), resolver.getOptionalDependents("B"),
            "B should have A as dependent");
        assertEquals(List.of("A"), resolver.getOptionalDependents("C"),
            "C should have A as dependent");
        assertTrue(resolver.getOptionalDependents("A").isEmpty(), "A has no dependents");
    }

    @Test
    void testNestedDependencies() {
        var pluginA = createpluginDescriptor("A", List.of(
            new PluginDependency("B?")
        ));
        var pluginB = createpluginDescriptor("B", List.of(
            new PluginDependency("C?")
        ));
        var pluginC = createpluginDescriptor("C", List.of());

        OptionalDependentResolver resolver =
            new OptionalDependentResolver(List.of(pluginA, pluginB, pluginC));

        assertEquals(List.of("B"), resolver.getOptionalDependents("C"),
            "C should have B as dependent");
        assertEquals(List.of("A"), resolver.getOptionalDependents("B"),
            "B should have A as dependent");
        assertTrue(resolver.getOptionalDependents("A").isEmpty(), "A has no dependents");
    }

    @Test
    void testCircularDependencies() {
        var pluginA = createpluginDescriptor("A", List.of(
            new PluginDependency("B?")
        ));
        var pluginB = createpluginDescriptor("B", List.of(
            new PluginDependency("A?")
        ));
        OptionalDependentResolver resolver =
            new OptionalDependentResolver(List.of(pluginA, pluginB));

        assertEquals(List.of("B"), resolver.getOptionalDependents("A"),
            "A should have B as dependent");
        assertEquals(List.of("A"), resolver.getOptionalDependents("B"),
            "B should have A as dependent");
    }

    @Test
    void testNonOptionalDependencies() {
        var pluginA = createpluginDescriptor("A", List.of(
            new PluginDependency("B")
        ));
        var pluginB = createpluginDescriptor("B", List.of());
        OptionalDependentResolver resolver =
            new OptionalDependentResolver(List.of(pluginA, pluginB));

        assertTrue(resolver.getOptionalDependents("B").isEmpty(), "B should have no dependents");
        assertTrue(resolver.getOptionalDependents("A").isEmpty(), "A should have no dependents");
    }

    PluginDescriptor createpluginDescriptor(String pluginName,
        List<PluginDependency> dependencies) {
        var descriptor = mock(PluginDescriptor.class);
        lenient().when(descriptor.getPluginId()).thenReturn(pluginName);
        lenient().when(descriptor.getDependencies()).thenReturn(dependencies);
        return descriptor;
    }
}
