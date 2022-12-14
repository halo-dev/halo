package run.halo.app.theme.router.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.theme.DefaultTemplateEnum;

/**
 * Tests for {@link AuthorRouteStrategy}.
 *
 * @author guqing
 * @since 2.0.1
 */
class AuthorRouteStrategyTest extends RouterStrategyTestSuite {

    @Mock
    private ReactiveExtensionClient client;

    @InjectMocks
    private AuthorRouteStrategy strategy;

    @Test
    void handlerTest() {
        User user = new User();
        Metadata metadata = new Metadata();
        metadata.setName("fake-user");
        user.setMetadata(metadata);
        user.setSpec(new User.UserSpec());

        when(client.fetch(eq(User.class), eq("fake-user"))).thenReturn(Mono.just(user));
        permalinkHttpGetRouter.insert("/authors/fake-user",
            strategy.getHandler(getThemeRouteRules(), "fake-user"));

        when(viewResolver.resolveViewName(eq(DefaultTemplateEnum.AUTHOR.getValue()), any()))
            .thenReturn(Mono.just(new EmptyView() {
                @Override
                public Mono<Void> render(Map<String, ?> model, MediaType contentType,
                    ServerWebExchange exchange) {
                    assertThat(model.get("name")).isEqualTo("fake-user");
                    assertThat(model.get("_templateId"))
                        .isEqualTo(DefaultTemplateEnum.AUTHOR.getValue());
                    assertThat(model.get("author")).isNotNull();
                    assertThat(model.get("posts")).isNotNull();
                    return Mono.empty();
                }
            }));

        WebTestClient webTestClient = getWebTestClient(getRouterFunction());
        webTestClient.get()
            .uri("/authors/fake-user")
            .exchange()
            .expectStatus()
            .isOk();
    }
}