package run.halo.app.theme.router.strategy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;
import reactor.core.publisher.Mono;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.properties.HaloProperties;
import run.halo.app.theme.router.PermalinkHttpGetRouter;

/**
 * Abstract test for {@link DetailsPageRouteHandlerStrategy} and
 * {@link ListPageRouteHandlerStrategy}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
abstract class RouterStrategyTestSuite {
    @Mock
    protected SystemConfigurableEnvironmentFetcher environmentFetcher;
    @Mock
    protected ApplicationContext applicationContext;
    @Mock
    protected HaloProperties haloProperties;

    @Mock
    protected ViewResolver viewResolver;

    @InjectMocks
    protected PermalinkHttpGetRouter permalinkHttpGetRouter;

    @BeforeEach
    final void setUpParent() throws URISyntaxException {
        lenient().when(environmentFetcher.fetch(eq(SystemSetting.ThemeRouteRules.GROUP),
            eq(SystemSetting.ThemeRouteRules.class))).thenReturn(Mono.just(getThemeRouteRules()));
        lenient().when(haloProperties.getExternalUrl()).thenReturn(new URI("http://example.com"));
        when(viewResolver.resolveViewName(any(), any()))
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

    public RouterFunction<ServerResponse> getRouterFunction() {
        return request -> Mono.justOrEmpty(permalinkHttpGetRouter.route(request));
    }
}
