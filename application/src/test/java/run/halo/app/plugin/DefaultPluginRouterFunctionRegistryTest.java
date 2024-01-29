package run.halo.app.plugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * Tests for {@link DefaultPluginRouterFunctionRegistry}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class DefaultPluginRouterFunctionRegistryTest {

    @InjectMocks
    DefaultPluginRouterFunctionRegistry routerFunctionRegistry;

    @Test
    void shouldRegisterRouterFunction() {
        RouterFunction<ServerResponse> routerFunction = mock(InvocationOnMock::getMock);
        routerFunctionRegistry.register(Set.of(routerFunction));
        assertEquals(Set.of(routerFunction), routerFunctionRegistry.getRouterFunctions());
    }

}
