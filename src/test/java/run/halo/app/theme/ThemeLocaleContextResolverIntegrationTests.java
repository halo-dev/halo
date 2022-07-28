package run.halo.app.theme;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.reactive.config.ViewResolverRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurationSupport;
import org.springframework.web.reactive.result.view.View;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.i18n.FixedLocaleContextResolver;
import org.springframework.web.server.i18n.LocaleContextResolver;
import reactor.core.publisher.Mono;

/**
 * @author guqing
 * @since 2.0.0
 */
class ThemeLocaleContextResolverIntegrationTests {

    private WebTestClient webTestClient;

    void fixedLocale() {
        webTestClient
            .get()
            .uri("/")
            .exchange()
            .expectStatus().isOk();

        // StepVerifier.create(result)
        //     .consumeNextWith(entity -> {
        //         assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
        //         assertThat(entity.getHeaders().getContentLanguage()).isEqualTo(Locale.GERMANY);
        //     })
        //     .verifyComplete();
    }


    @TestConfiguration
    @ComponentScan(resourcePattern = "**/ThemeLocaleContextResolverIntegrationTests*.class")
    @SuppressWarnings({"unused", "WeakerAccess"})
    static class WebConfig extends WebFluxConfigurationSupport {

        @Override
        protected LocaleContextResolver createLocaleContextResolver() {
            return new FixedLocaleContextResolver(Locale.GERMANY);
        }

        @Override
        protected void configureViewResolvers(ViewResolverRegistry registry) {
            registry.viewResolver((viewName, locale) -> Mono.just(new DummyView(locale)));
        }

        private static class DummyView implements View {

            private final Locale locale;

            public DummyView(Locale locale) {
                this.locale = locale;
            }

            @Override
            public List<MediaType> getSupportedMediaTypes() {
                return Collections.singletonList(MediaType.TEXT_HTML);
            }

            @Override
            public Mono<Void> render(@Nullable Map<String, ?> model,
                @Nullable MediaType contentType,
                ServerWebExchange exchange) {
                exchange.getResponse().getHeaders().setContentLanguage(locale);
                return Mono.empty();
            }
        }
    }

    @Controller
    @SuppressWarnings("unused")
    static class TestController {

        @GetMapping("/")
        public String foo() {
            return "foo";
        }
    }
}
