package run.halo.app.config;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest
@AutoConfigureWebTestClient
class CorsTest {

    @Autowired
    WebTestClient webClient;

    @Nested
    class RequestCorsEnabledApi {

        @Test
        @WithMockUser
        void shouldNotResponseAllowOriginHeaderWithSameOrigin() {
            webClient.get().uri("http://localhost:3000/apis/cors-enabled")
                .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                .header(HttpHeaders.AUTHORIZATION, "fake-authorization")
                .header("FakeHeader", "fake-header-value")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectHeader()
                .doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN);
        }

        @Test
        @WithMockUser
        void shouldResponseAllowOriginHeaderWithDifferentOrigin() {
            webClient.get().uri("http://localhost:3000/apis/cors-enabled")
                .header(HttpHeaders.ORIGIN, "https://another.website")
                .header(HttpHeaders.AUTHORIZATION, "fake-authorization")
                // .header("ForbiddenHeader", "fake value")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectHeader()
                .exists(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN);
        }

        @Test
        @WithMockUser
        void shouldResponseAllowOriginHeaderWithForbiddenHeader() {
            webClient.get().uri("http://localhost:3000/apis/cors-enabled")
                .header(HttpHeaders.ORIGIN, "https://another.website")
                .header(HttpHeaders.AUTHORIZATION, "fake-authorization")
                .header("FakeHeader", "fake-header-value")
                // .header("ForbiddenHeader", "fake value")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectHeader()
                .exists(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN);
        }
    }

    @Nested
    class RequestCorsDisabledApi {

        @Test
        @WithMockUser
        void shouldNotResponseAllowOriginHeaderWithDifferentOrigin() {
            webClient.get().uri("http://localhost:3000/cors-disabled")
                .header(HttpHeaders.ORIGIN, "https://another.website")
                .header(HttpHeaders.AUTHORIZATION, "fake-authorization")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectHeader()
                .doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN);
        }

        @Test
        @WithMockUser
        void shouldNotResponseAllowOriginHeaderWithSameOrigin() {
            webClient.get().uri("http://localhost:3000/cors-disabled")
                .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                .header(HttpHeaders.AUTHORIZATION, "fake-authorization")
                .header("FakeHeader", "fake-header-value")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectHeader()
                .doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN);
        }
    }

}
