package run.halo.app.theme.config;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;
import static run.halo.app.theme.ThemeLocaleContextResolver.LANGUAGE_COOKIE_NAME;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;

@SpringBootTest
@AutoConfigureWebTestClient
class ChangeLocaleTest {

    @Autowired
    WebTestClient webClient;

    @Test
    void shouldRedirectToIndexIfNoRefererHeader() {
        var formData = new LinkedMultiValueMap<String, String>();
        formData.put("language", List.of("zh-CN"));
        webClient.mutate().apply(csrf()).build()
            .post().uri("/locale")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue(formData)
            .exchange()
            .expectStatus().isFound()
            .expectHeader().location("/")
            .expectCookie().valueEquals(LANGUAGE_COOKIE_NAME, "zh-CN");
    }

    @Test
    void shouldRedirectToRefererIfRefererHeaderProvided() {
        var formData = new LinkedMultiValueMap<String, String>();
        formData.put("language", List.of("zh-CN"));
        webClient.mutate().apply(csrf()).build()
            .post().uri("/locale")
            .header(HttpHeaders.REFERER, "/path?query#fragment")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue(formData)
            .exchange()
            .expectStatus().isFound()
            .expectHeader().location("/path?query#fragment")
            .expectCookie().valueEquals(LANGUAGE_COOKIE_NAME, "zh-CN");
    }

    @Test
    void shouldRespondUndWhenLanguageIsInvalid() {
        var formData = new LinkedMultiValueMap<String, String>();
        formData.put("language", List.of("invalid_language"));
        webClient.mutate().apply(csrf()).build()
            .post().uri("/locale")
            .header(HttpHeaders.REFERER, "/path?query#fragment")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue(formData)
            .exchange()
            .expectStatus().isFound()
            .expectHeader().location("/path?query#fragment")
            .expectCookie().valueEquals(LANGUAGE_COOKIE_NAME, "und");
    }

    @Test
    void shouldRespondBadRequestWhenLanguageIsNotProvided() {
        var formData = new LinkedMultiValueMap<String, String>();
        webClient.mutate().apply(csrf()).build()
            .post().uri("/locale")
            .header(HttpHeaders.REFERER, "/path?query#fragment")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue(formData)
            .exchange()
            .expectStatus().isBadRequest();
    }
}
