package run.halo.app.core.extension.endpoint;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.reactive.server.WebTestClient.bindToRouterFunction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.SystemInitializationEndpoint.SystemInitializationRequest;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.InitializationStateGetter;
import run.halo.app.infra.SystemSetting;
import run.halo.app.security.SuperAdminInitializer;
import run.halo.app.security.SuperAdminInitializer.InitializationParam;

/**
 * Tests for {@link SystemInitializationEndpoint}.
 *
 * @author guqing
 * @since 2.9.0
 */
@ExtendWith(MockitoExtension.class)
class SystemInitializationEndpointTest {

    @Mock
    InitializationStateGetter initializationStateGetter;

    @Mock
    SuperAdminInitializer superAdminInitializer;

    @Mock
    ReactiveExtensionClient client;

    @InjectMocks
    SystemInitializationEndpoint initializationEndpoint;

    WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = bindToRouterFunction(initializationEndpoint.endpoint()).build();
    }

    @Test
    void initializeWithoutRequestBody() {
        webTestClient.post()
            .uri("/system/initialize")
            .exchange()
            .expectStatus()
            .isBadRequest();
    }

    @Test
    void initializeWithRequestBody() {
        var initialization = new SystemInitializationRequest();
        initialization.setUsername("faker");
        initialization.setPassword("openfaker");
        initialization.setEmail("faker@halo.run");
        initialization.setSiteTitle("Fake Site");

        when(initializationStateGetter.userInitialized()).thenReturn(Mono.just(false));
        when(superAdminInitializer.initialize(any(InitializationParam.class)))
            .thenReturn(Mono.empty());

        var configMap = new ConfigMap();
        when(client.get(ConfigMap.class, SystemSetting.SYSTEM_CONFIG))
            .thenReturn(Mono.just(configMap));
        when(client.update(configMap)).thenReturn(Mono.just(configMap));

        webTestClient.post().uri("/system/initialize")
            .bodyValue(initialization)
            .exchange()
            .expectStatus().isCreated()
            .expectHeader().location("/console");

        verify(initializationStateGetter).userInitialized();
        verify(superAdminInitializer).initialize(any());
        verify(client).get(ConfigMap.class, SystemSetting.SYSTEM_CONFIG);
        verify(client).update(configMap);
    }
}
