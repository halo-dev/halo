package run.halo.app.theme;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers;

import java.util.LinkedHashSet;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher.MatchResult;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Menu;
import run.halo.app.core.extension.MenuItem;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.Metadata;
import run.halo.app.infra.InitializationStateGetter;
import run.halo.app.infra.utils.HaloUtils;
import run.halo.app.security.AfterSecurityWebFilter;
import run.halo.app.theme.router.ModelConst;

@SpringBootTest
@Import(ThemeIntegrationTest.TestConfig.class)
@AutoConfigureWebTestClient
@DirtiesContext
public class ThemeIntegrationTest {

    @Autowired
    WebTestClient webClient;

    @MockitoBean
    InitializationStateGetter initializationStateGetter;

    @Autowired
    ExtensionClient client;

    @BeforeEach
    void setUp() {
        when(initializationStateGetter.userInitialized()).thenReturn(Mono.just(true));

        // create a menu item
        var menuItem = new MenuItem();
        menuItem.setMetadata(new Metadata());
        menuItem.getMetadata().setName("main-menu-home");
        menuItem.setSpec(new MenuItem.MenuItemSpec());
        menuItem.getSpec().setDisplayName("Home");
        menuItem.getSpec().setHref("/");
        client.create(menuItem);

        // create a primary menu
        var menu = new Menu();
        menu.setMetadata(new Metadata());
        menu.getMetadata().setName("main-menu");
        menu.setSpec(new Menu.Spec());
        menu.getSpec().setDisplayName("Mail Menu");
        menu.getSpec().setMenuItems(new LinkedHashSet<>(List.of("main-menu-home")));
        client.create(menu);
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        RouterFunction<ServerResponse> noTemplateExistsRoute() {
            return RouterFunctions.route()
                .GET(
                    "/no-template-exists",
                    request -> ServerResponse.ok().render("no-template-exists")
                )
                .build();
        }

        @Bean
        RouterFunction<ServerResponse> noCacheRoute() {
            return RouterFunctions.route()
                .GET(
                    "/should-not-cache",
                    request -> ServerResponse.ok().render("no-template-exists")
                )
                .before(HaloUtils.noCache())
                .build();
        }

        @Bean
        AfterSecurityWebFilter poweredByHaloTemplateEngineCheckFilter() {
            var matcher = pathMatchers(HttpMethod.GET, "/should-not-cache");
            return (exchange, chain) -> chain.filter(exchange)
                .flatMap(v -> matcher.matches(exchange)
                    .filter(MatchResult::isMatch)
                    .switchIfEmpty(Mono.fromRunnable(() -> {
                        assertNull(exchange.getAttribute(ModelConst.NO_CACHE));
                        assertTrue(exchange.getRequiredAttribute(
                            ModelConst.POWERED_BY_HALO_TEMPLATE_ENGINE)
                        );
                    }).then(Mono.empty()))
                    .doOnNext(m -> {
                        assertTrue(exchange.getRequiredAttribute(ModelConst.NO_CACHE));
                        assertFalse(exchange.getRequiredAttribute(
                            ModelConst.POWERED_BY_HALO_TEMPLATE_ENGINE)
                        );
                    })
                )
                .then();
        }

    }

    @Test
    void shouldRespondNotFoundIfNoTemplateFound() {
        webClient.get()
            .uri("/no-template-exists")
            .accept(MediaType.TEXT_HTML)
            .exchange()
            .expectStatus().isNotFound()
            .expectBody(String.class)
            .value(Matchers.containsString("Template no-template-exists was not found"));

        webClient.get()
            .uri("/should-not-cache")
            .accept(MediaType.TEXT_HTML)
            .exchange()
            .expectStatus().isNotFound()
            .expectBody(String.class)
            .value(Matchers.containsString("Template no-template-exists was not found"));
    }

}
