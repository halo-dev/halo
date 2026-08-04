package run.halo.app.security.preauth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import run.halo.app.security.authentication.oauth2.OAuth2AuthenticationTokenCache;

@SpringBootTest
@AutoConfigureWebTestClient
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class OAuth2SelectPageIntegrationTest {

    @Autowired
    WebTestClient webClient;

    @MockitoBean
    OAuth2AuthenticationTokenCache tokenCache;

    @Test
    void shouldRenderSelectPageWhenOAuth2TokenCached() {
        var user = new DefaultOAuth2User(
                java.util.List.of(new SimpleGrantedAuthority("ROLE_authenticated")),
                Map.of("sub", "alice", "email", "alice@example.com"),
                "sub");
        when(tokenCache.getToken(any()))
                .thenReturn(Mono.just(new OAuth2AuthenticationToken(user, java.util.List.of(), "github")));

        webClient
                .get()
                .uri("/login?oauth2_select")
                .header(HttpHeaders.ACCEPT_LANGUAGE, "zh-CN")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .value(body -> org.assertj.core.api.Assertions.assertThat(body).contains("选择登录方式"));
    }
}
