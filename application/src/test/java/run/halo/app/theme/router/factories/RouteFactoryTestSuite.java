package run.halo.app.theme.router.factories;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;

import java.net.URISyntaxException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;
import reactor.core.publisher.Mono;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.theme.router.EmptyView;

/**
 * Abstract test for {@link RouteFactory}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
abstract class RouteFactoryTestSuite {
    @Mock
    protected SystemConfigurableEnvironmentFetcher environmentFetcher;
    @Mock
    protected ViewResolver viewResolver;

    @BeforeEach
    final void setUpParent() throws URISyntaxException {
        lenient().when(environmentFetcher.fetchPost())
            .thenReturn(Mono.just(new SystemSetting.Post()));
        lenient().when(environmentFetcher.fetch(eq(SystemSetting.ThemeRouteRules.GROUP),
            eq(SystemSetting.ThemeRouteRules.class))).thenReturn(Mono.just(getThemeRouteRules()));
        lenient().when(viewResolver.resolveViewName(any(), any()))
            .thenReturn(Mono.just(new EmptyView()));
        setUp();
    }

    public void setUp() {

    }

    public SystemSetting.ThemeRouteRules getThemeRouteRules() {
        SystemSetting.ThemeRouteRules themeRouteRules = new SystemSetting.ThemeRouteRules();
        themeRouteRules.setArchives("archives");
        themeRouteRules.setPost("/archives/{slug}");
        themeRouteRules.setTags("tags");
        themeRouteRules.setCategories("categories");
        return themeRouteRules;
    }

    public WebTestClient getWebTestClient(RouterFunction<ServerResponse> routeFunction) {
        return WebTestClient.bindToRouterFunction(routeFunction)
            .handlerStrategies(HandlerStrategies.builder()
                .viewResolver(viewResolver)
                .build())
            .build();
    }
}
