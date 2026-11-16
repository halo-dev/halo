package run.halo.app.plugin;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * Tests for {@link SharedApplicationContextFactory}.
 *
 * @author guqing
 * @since 2.0.0
 */
@SpringBootTest
class SharedApplicationContextFactoryTest {

    @Autowired
    ApplicationContext applicationContext;

    @Test
    void createSharedApplicationContext() {
        var sharedContext = SharedApplicationContextFactory.create(applicationContext);
        assertNotNull(sharedContext);
        var bean = sharedContext.getBean("pluginRouteVisitor");
        assertFalse(bean instanceof RouterFunction<?>);
        @SuppressWarnings("unchecked")
        var inspect = (Consumer<RouterFunctions.Visitor>) bean;
        var visitor = mock(RouterFunctions.Visitor.class);
        var registry = applicationContext.getBean(PluginRouterFunctionRegistry.class);
        @SuppressWarnings("unchecked")
        RouterFunction<ServerResponse> route = mock(RouterFunction.class);
        registry.register(List.of(route));
        try {
            inspect.accept(visitor);
            verify(route).accept(visitor);
        } finally {
            registry.unregister(List.of(route));
            ((ConfigurableApplicationContext) sharedContext).close();
        }
    }
}
