// package run.halo.app.theme.router;
//
// import static org.assertj.core.api.Assertions.assertThat;
// import static org.mockito.ArgumentMatchers.eq;
// import static org.mockito.Mockito.when;
//
// import java.util.Map;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.Mock;
// import org.mockito.Mockito;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.springframework.context.ApplicationContext;
// import org.springframework.web.reactive.function.server.RouterFunction;
// import org.springframework.web.reactive.function.server.ServerResponse;
// import run.halo.app.theme.DefaultTemplateEnum;
// import run.halo.app.theme.finders.TagFinder;
// import run.halo.app.theme.router.strategy.TagsRouteStrategy;
//
// /**
//  * Tests for {@link TemplateRouteManager}.
//  *
//  * @author guqing
//  * @since 2.0.0
//  */
// @ExtendWith(MockitoExtension.class)
// class TemplateRouteManagerTest {
//
//     @Mock
//     private PermalinkPatternProvider permalinkPatternProvider;
//
//     @Mock
//     private ApplicationContext applicationContext;
//
//     private TemplateRouteManager templateRouteManager;
//
//     @BeforeEach
//     void setUp() {
//         templateRouteManager = new TemplateRouteManager(permalinkPatternProvider,
//             applicationContext);
//
//         when(permalinkPatternProvider.getPattern(DefaultTemplateEnum.TAGS))
//             .thenReturn("/tags");
//
//         TagFinder tagFinder = Mockito.mock(TagFinder.class);
//         when(applicationContext.getBean(eq(TagsRouteStrategy.class)))
//             .thenReturn(new TagsRouteStrategy(tagFinder));
//
//         templateRouteManager.register(DefaultTemplateEnum.TAGS.getValue());
//     }
//
//     @Test
//     void getRouterFunctionMap() {
//         Map<String, RouterFunction<ServerResponse>> routerFunctionMap =
//             templateRouteManager.getRouterFunctionMap();
//         assertThat(routerFunctionMap.size()).isEqualTo(1);
//         assertThat(routerFunctionMap).containsKey(DefaultTemplateEnum.TAGS.getValue());
//     }
//
//     @Test
//     void changeTemplatePattern() {
//         Map<String, RouterFunction<ServerResponse>> routerFunctionMap =
//             templateRouteManager.getRouterFunctionMap();
//
//         RouterFunction<ServerResponse> routerFunction =
//             routerFunctionMap.get(DefaultTemplateEnum.TAGS.getValue());
//
//         when(permalinkPatternProvider.getPattern(DefaultTemplateEnum.TAGS))
//             .thenReturn("/t");
//         RouterFunction<ServerResponse> newRouterFunction =
//             templateRouteManager.changeTemplatePattern(DefaultTemplateEnum.TAGS.getValue());
//
//         assertThat(newRouterFunction).isNotEqualTo(routerFunction);
//     }
// }