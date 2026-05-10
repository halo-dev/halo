package run.halo.app.theme.finders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

/**
 * Tests for {@link FinderRegistry}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class FinderRegistryTest {

    private DefaultFinderRegistry finderRegistry;

    @Mock
    private ApplicationContext applicationContext;

    @BeforeEach
    void setUp() {
        finderRegistry = new DefaultFinderRegistry(applicationContext);
    }

    @Test
    void registerFinder() {
        assertThatThrownBy(() -> {
                    finderRegistry.putFinder(new Object());
                })
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Finder must be annotated with @Finder");

        String s = finderRegistry.putFinder(new FakeFinder());
        assertThat(s).isEqualTo("test");
    }

    @Test
    void removeFinder() {
        String s = finderRegistry.putFinder(new FakeFinder());
        assertThat(s).isEqualTo("test");
        Object test = finderRegistry.get("test");
        assertThat(test).isNotNull();
        finderRegistry.removeFinder(s);

        test = finderRegistry.get("test");
        assertThat(test).isNull();
    }

    @Test
    void getFinders() {
        assertThat(finderRegistry.getFinders()).hasSize(0);

        finderRegistry.putFinder(new FakeFinder());
        Map<String, Object> finders = finderRegistry.getFinders();
        assertThat(finders).hasSize(1);
    }

    @Test
    void registerAndUnregisterPlugin() {
        var pluginContext = org.mockito.Mockito.mock(ApplicationContext.class);
        var finder = new FakeFinder();
        when(pluginContext.getBeansWithAnnotation(Finder.class)).thenReturn(Map.of("fakeFinder", finder));

        finderRegistry.register("plugin-a", pluginContext);
        assertThat(finderRegistry.get("test")).isNotNull();

        finderRegistry.unregister("plugin-a");
        assertThat(finderRegistry.get("test")).isNull();
    }

    @Test
    void reRegisterCleansStaleFinders() {
        var pluginContext = org.mockito.Mockito.mock(ApplicationContext.class);
        var finder1 = new FakeFinder();
        when(pluginContext.getBeansWithAnnotation(Finder.class)).thenReturn(Map.of("fakeFinder", finder1));

        // First registration (simulates a previous plugin start that registered finders)
        finderRegistry.register("plugin-a", pluginContext);
        assertThat(finderRegistry.get("test")).isEqualTo(finder1);

        // Simulate stale state: unregister was never called (e.g., context closed without
        // firing ContextClosedEvent due to a failed refresh after ContextRefreshedEvent)
        // Now re-register with new finders (e.g., after plugin upgrade)
        var newPluginContext = org.mockito.Mockito.mock(ApplicationContext.class);
        var finder2 = new FakeFinder();
        when(newPluginContext.getBeansWithAnnotation(Finder.class)).thenReturn(Map.of("fakeFinder", finder2));

        // register should not throw "Finder with name 'test' is already registered"
        finderRegistry.register("plugin-a", newPluginContext);
        // The new finder should be registered
        assertThat(finderRegistry.get("test")).isEqualTo(finder2);
    }

    @Finder("test")
    static class FakeFinder {}
}
