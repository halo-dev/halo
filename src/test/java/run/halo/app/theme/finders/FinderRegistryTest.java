package run.halo.app.theme.finders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pf4j.PluginWrapper;
import org.springframework.context.ApplicationContext;
import run.halo.app.plugin.event.HaloPluginStoppedEvent;

/**
 * Tests for {@link FinderRegistry}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class FinderRegistryTest {

    private FinderRegistry finderRegistry;
    @Mock
    private ApplicationContext applicationContext;

    @BeforeEach
    void setUp() {
        finderRegistry = new FinderRegistry(applicationContext);
    }

    @Test
    void registerFinder() {
        assertThatThrownBy(() -> {
            finderRegistry.registerFinder(new Object());
        }).isInstanceOf(IllegalStateException.class)
            .hasMessage("Finder must be annotated with @Finder");

        String s = finderRegistry.registerFinder(new FakeFinder());
        assertThat(s).isEqualTo("test");
    }

    @Test
    void removeFinder() {
        String s = finderRegistry.registerFinder(new FakeFinder());
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

        finderRegistry.registerFinder(new FakeFinder());
        Map<String, Object> finders = finderRegistry.getFinders();
        assertThat(finders).hasSize(1);
    }

    @Test
    void onPluginStopped() {
        finderRegistry.registerFinder("a", new Object());
        finderRegistry.addPluginFinder("fake", "a");

        HaloPluginStoppedEvent event = Mockito.mock(HaloPluginStoppedEvent.class);
        PluginWrapper pluginWrapper = Mockito.mock(PluginWrapper.class);
        when(event.getPlugin()).thenReturn(pluginWrapper);
        when(pluginWrapper.getPluginId()).thenReturn("fake");

        finderRegistry.onPluginStopped(event);
        assertThat(finderRegistry.get("a")).isNull();
    }

    @Finder("test")
    static class FakeFinder {

    }
}