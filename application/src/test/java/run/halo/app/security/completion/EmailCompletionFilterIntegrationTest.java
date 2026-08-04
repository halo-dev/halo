package run.halo.app.security.completion;

import static org.mockito.Mockito.when;

import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;

@SpringBootTest
@AutoConfigureWebTestClient
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@WithMockUser(username = "fake-user", roles = "authenticated")
class EmailCompletionFilterIntegrationTest {

    @Autowired
    WebTestClient webClient;

    @Autowired
    ReactiveExtensionClient client;

    @MockitoSpyBean
    SystemConfigFetcher systemConfigFetcher;

    @BeforeEach
    void setUp() {
        var setting = new SystemSetting.User();
        setting.setMustVerifyEmailOnRegistration(true);
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(setting));
        createUser("fake-user", null, false);
    }

    void createUser(String name, String email, boolean verified) {
        var user = new User();
        user.setMetadata(new Metadata());
        user.getMetadata().setName(name);
        var spec = new User.UserSpec();
        spec.setDisplayName("Fake User");
        spec.setEmail(email);
        spec.setEmailVerified(verified);
        spec.setRegisteredAt(Instant.now());
        user.setSpec(spec);
        client.create(user).block();
    }

    @Test
    void shouldRedirectHtmlRequestToCompleteProfile() {
        webClient
                .get()
                .uri("/uc")
                .accept(MediaType.TEXT_HTML)
                .exchange()
                .expectStatus()
                .isFound()
                .expectHeader()
                .location("/complete-profile");
    }

    @Test
    void shouldReturnForbiddenForJsonRequest() {
        webClient
                .get()
                .uri("/apis/api.console.halo.run/v1alpha1/users")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isForbidden()
                .expectBody()
                .jsonPath("$.type")
                .isEqualTo("email-not-set");
    }
}
