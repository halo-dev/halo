package run.halo.app.plugin;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pf4j.PluginWrapper;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import run.halo.app.search.SearchService;

@SpringBootTest
class DefaultPluginApplicationContextFactoryTest {

    @MockitoSpyBean
    SpringPluginManager pluginManager;

    DefaultPluginApplicationContextFactory factory;

    @BeforeEach
    void setUp() {
        factory = new DefaultPluginApplicationContextFactory(pluginManager);
    }

    @Test
    void shouldCreateCorrectly() {
        var pw = mock(PluginWrapper.class);
        when(pw.getPluginClassLoader()).thenReturn(this.getClass().getClassLoader());
        var sp = mock(SpringPlugin.class);
        var pluginContext = new PluginContext.PluginContextBuilder()
            .name("fake-plugin")
            .version("1.0.0")
            .build();
        when(sp.getPluginContext()).thenReturn(pluginContext);
        when(pw.getPlugin()).thenReturn(sp);
        when(pluginManager.getPlugin("fake-plugin")).thenReturn(pw);
        var context = factory.create("fake-plugin");

        assertInstanceOf(PluginApplicationContext.class, context);
        assertNotNull(context.getBeanProvider(SearchService.class).getIfUnique());
        assertNotNull(context.getBeanProvider(PluginsRootGetter.class).getIfUnique());
        // TODO Add more assertions here.
    }

}