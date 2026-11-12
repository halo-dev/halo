package run.halo.app.security.preauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.nio.file.Path;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.RoleBinding;
import run.halo.app.core.extension.User;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.InitializationStateGetter;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.SystemState;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "halo.security.basic-auth.disabled=false")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SystemSetupIntegrationTest {

    @TempDir
    static Path workDir;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        var instanceDirectory = workDir.resolve(UUID.randomUUID().toString());
        registry.add("halo.work-dir", instanceDirectory::toString);
    }

    @Autowired
    ReactiveExtensionClient client;

    @Autowired
    InitializationStateGetter initializationStateGetter;

    @Autowired
    PasswordEncoder passwordEncoder;

    @MockitoSpyBean
    SystemConfigFetcher systemConfigFetcher;

    @LocalServerPort
    int port;

    WebTestClient http;

    @BeforeEach
    void setUp() {
        http = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .responseTimeout(Duration.ofSeconds(30))
                .build();
    }

    @Test
    void shouldRejectReservedUsernameAndRequireOriginalPasswordToResume() {
        assertThat(initializationStateGetter.setupRequired().block()).isTrue();
        setup("ghost", "Original123!")
                .expectStatus()
                .isBadRequest()
                .expectBody()
                .jsonPath("$.errors[0]")
                .value(message -> assertThat(message.toString()).contains("username already exists"));
        assertThat(client.fetch(ConfigMap.class, SystemState.SYSTEM_STATES_CONFIGMAP)
                        .block())
                .isNull();
        assertThat(client.fetch(RoleBinding.class, "ghost-super-role-binding").block())
                .isNull();

        // Interrupt setup after the real administrator and binding have been persisted.
        doReturn(Mono.error(new IllegalStateException("Basic config temporarily unavailable")))
                .when(systemConfigFetcher)
                .getConfigMap();
        setup("setup-admin", "Original123!").expectStatus().is5xxServerError();
        var admin = client.get(User.class, "setup-admin").block();
        assertThat(admin).isNotNull();
        assertThat(passwordEncoder.matches("Original123!", admin.getSpec().getPassword()))
                .isTrue();
        assertThat(readState().getIsSetup()).isFalse();
        http.get()
                .uri("/system/setup")
                .accept(MediaType.TEXT_HTML)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("setup-admin").contains("readonly"));

        doCallRealMethod().when(systemConfigFetcher).getConfigMap();
        clearInvocations(systemConfigFetcher);
        setup("setup-admin", "Different123!").expectStatus().isBadRequest();
        setup("setup-admin", "Different123!", MediaType.TEXT_HTML)
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("incorrect"));
        verify(systemConfigFetcher, never()).getConfigMap();
        assertThat(client.get(ConfigMap.class, SystemSetting.SYSTEM_CONFIG)
                        .block()
                        .getData())
                .doesNotContainKey(SystemSetting.Basic.GROUP);
        assertThat(readState().getIsSetup()).isFalse();

        setup("setup-admin", "Original123!").expectStatus().isNoContent();
        assertThat(readState().getIsSetup()).isTrue();
        assertThat(client.get(RoleBinding.class, "setup-admin-super-role-binding")
                        .block())
                .isNotNull();
        assertThat(client.get(User.class, "setup-admin").block().getSpec().getPassword())
                .isEqualTo(admin.getSpec().getPassword());
        http.get()
                .uri("/system/setup")
                .accept(MediaType.TEXT_HTML)
                .exchange()
                .expectStatus()
                .isFound()
                .expectHeader()
                .location("/console");
        http.get()
                .uri("/apis/uc.api.halo.run/v1alpha1/users/-")
                .headers(headers -> headers.setBasicAuth("setup-admin", "Original123!"))
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    void shouldKeepLegacyInstallationClosedWithoutBackfill() {
        var legacyAdmin = new User();
        legacyAdmin.setMetadata(new Metadata());
        legacyAdmin.getMetadata().setName("legacy-admin");
        legacyAdmin.setSpec(new User.UserSpec());
        legacyAdmin.getSpec().setDisplayName("Legacy administrator");
        legacyAdmin.getSpec().setPassword(passwordEncoder.encode("Original123!"));
        client.create(legacyAdmin).block();

        assertThat(client.fetch(ConfigMap.class, SystemState.SYSTEM_STATES_CONFIGMAP)
                        .block())
                .isNull();
        assertThat(initializationStateGetter.setupRequired().block()).isFalse();
        setup("new-admin", "Original123!").expectStatus().isFound();
        assertThat(client.fetch(User.class, "new-admin").block()).isNull();
        assertThat(client.fetch(ConfigMap.class, SystemState.SYSTEM_STATES_CONFIGMAP)
                        .block())
                .isNull();
        http.get()
                .uri("/system/setup")
                .accept(MediaType.TEXT_HTML)
                .exchange()
                .expectStatus()
                .isFound()
                .expectHeader()
                .location("/console");
    }

    private WebTestClient.ResponseSpec setup(String username, String password) {
        return setup(username, password, MediaType.APPLICATION_JSON);
    }

    private WebTestClient.ResponseSpec setup(String username, String password, MediaType accept) {
        return http.post()
                .uri("/system/setup")
                .accept(accept)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("username", username)
                        .with("password", password)
                        .with("email", "admin@example.com")
                        .with("siteTitle", "Setup integration")
                        .with("language", "en")
                        .with("externalUrl", "https://setup.example.test"))
                .exchange();
    }

    private SystemState readState() {
        return SystemState.deserialize(
                client.get(ConfigMap.class, SystemState.SYSTEM_STATES_CONFIGMAP).block());
    }
}
