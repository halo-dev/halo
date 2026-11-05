package run.halo.app.security.preauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Unstructured;
import run.halo.app.infra.ExternalUrlChangedEvent;
import run.halo.app.infra.ExternalUrlSupplier;
import run.halo.app.infra.InitializationStateGetter;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.SystemState;
import run.halo.app.infra.exception.DuplicateNameException;
import run.halo.app.infra.utils.JsonUtils;
import run.halo.app.infra.utils.YamlUnstructuredLoader;
import run.halo.app.plugin.PluginService;
import run.halo.app.security.SuperAdminInitializer;
import run.halo.app.theme.service.ThemeService;

/**
 * Tests for {@link SystemSetupEndpoint}.
 *
 * @author guqing
 * @since 2.20.0
 */
class SystemSetupEndpointTest {

    private final InitializationStateGetter initializationStateGetter = mock(InitializationStateGetter.class);
    private final SystemConfigFetcher systemConfigFetcher = mock(SystemConfigFetcher.class);
    private final SuperAdminInitializer superAdminInitializer = mock(SuperAdminInitializer.class);
    private final ReactiveExtensionClient client = mock(ReactiveExtensionClient.class);
    private final PluginService pluginService = mock(PluginService.class);
    private final ThemeService themeService = mock(ThemeService.class);
    private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

    private SystemSetupEndpoint endpoint;

    private ConfigMap systemStates;

    private ConfigMap systemConfig;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        endpoint = new SystemSetupEndpoint(
                initializationStateGetter,
                systemConfigFetcher,
                superAdminInitializer,
                client,
                pluginService,
                themeService,
                mock(Validator.class),
                mock(ObjectProvider.class),
                mock(ExternalUrlSupplier.class),
                eventPublisher);

        systemStates = configMap(SystemState.SYSTEM_STATES_CONFIGMAP);
        systemConfig = configMap("system");

        when(client.fetch(ConfigMap.class, SystemState.SYSTEM_STATES_CONFIGMAP)).thenReturn(Mono.just(systemStates));
        when(client.update(any(ConfigMap.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(client.create(any(Unstructured.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(systemConfigFetcher.getConfigMap()).thenReturn(Mono.just(systemConfig));
        when(superAdminInitializer.initialize(any())).thenReturn(Mono.empty());
        when(pluginService.installPresetPlugins()).thenReturn(Mono.empty());
        when(themeService.installPresetTheme()).thenReturn(Mono.empty());
    }

    @Test
    void placeholderTest() {
        var properties = new Properties();
        properties.setProperty("username", "guqing");
        properties.setProperty("timestamp", "2024-09-30");
        var str = SystemSetupEndpoint.PROPERTY_PLACEHOLDER_HELPER.replacePlaceholders("""
            ${username}
            ${timestamp}
            """, properties);
        assertThat(str).isEqualTo("""
            guqing
            2024-09-30
            """);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldBindDefaultArchivesMenuItemToThemeRoute() {
        var extensions = new YamlUnstructuredLoader(new ClassPathResource("initial-data.yaml")).load();
        var archives = extensions.stream()
                .filter(extension -> "MenuItem".equals(extension.getKind()))
                .filter(extension -> "c4c814d1-0c2c-456b-8c96-4864965fee94"
                        .equals(extension.getMetadata().getName()))
                .findFirst()
                .orElseThrow();
        var spec = (Map<String, Object>) archives.getData().get("spec");

        assertThat(spec).containsEntry("routeRef", "archives").doesNotContainKey("href");
    }

    @Test
    void shouldInitializeSequentiallyAndMarkSetupCompletedLast() {
        StepVerifier.create(endpoint.doInitialization(setupRequest(), "admin")).verifyComplete();

        var systemState = SystemState.deserialize(systemStates);
        assertThat(systemState.getIsSetup()).isTrue();
        assertThat(systemState.getSetupUsername()).isEqualTo("admin");

        InOrder inOrder = inOrder(superAdminInitializer, systemConfigFetcher, pluginService, themeService);
        inOrder.verify(superAdminInitializer).initialize(any());
        inOrder.verify(systemConfigFetcher).getConfigMap();
        inOrder.verify(pluginService).installPresetPlugins();
        inOrder.verify(themeService).installPresetTheme();
    }

    @Test
    void shouldKeepSetupResumableWhenInitializationFails() {
        when(themeService.installPresetTheme())
                .thenReturn(Mono.error(new IllegalStateException("Failed to install preset theme")));

        StepVerifier.create(endpoint.doInitialization(setupRequest(), "admin"))
                .verifyError(IllegalStateException.class);

        var systemState = SystemState.deserialize(systemStates);
        assertThat(systemState.getIsSetup()).isFalse();
        assertThat(systemState.getSetupUsername()).isEqualTo("admin");
    }

    @Test
    void shouldIgnoreExistingPresetExtensions() {
        when(client.create(any(Unstructured.class)))
                .thenReturn(Mono.error(new DuplicateNameException("Duplicate name")));

        StepVerifier.create(endpoint.doInitialization(setupRequest(), "admin")).verifyComplete();

        assertThat(SystemState.deserialize(systemStates).getIsSetup()).isTrue();
    }

    @Test
    void shouldResolveUsernameRecordedByInterruptedSetup() {
        var interruptedState = new SystemState();
        interruptedState.setIsSetup(false);
        interruptedState.setSetupUsername("admin");
        SystemState.update(interruptedState, systemStates);

        StepVerifier.create(endpoint.resolveSetupUsername("another"))
                .expectNext("admin")
                .verifyComplete();
    }

    @Test
    void shouldUseSubmittedUsernameForFreshSetup() {
        StepVerifier.create(endpoint.resolveSetupUsername("admin"))
                .expectNext("admin")
                .verifyComplete();
    }

    @Test
    void shouldNotDowngradeCompletedSetup() {
        var completedState = new SystemState();
        completedState.setIsSetup(true);
        completedState.setSetupUsername("admin");
        SystemState.update(completedState, systemStates);

        StepVerifier.create(endpoint.doInitialization(setupRequest(), "admin")).verifyComplete();

        assertThat(SystemState.deserialize(systemStates).getIsSetup()).isTrue();
        verify(superAdminInitializer, never()).initialize(any());
        verify(themeService, never()).installPresetTheme();
    }

    @Test
    void shouldKeepSetupResumableWhenSystemConfigMapIsMissing() {
        when(systemConfigFetcher.getConfigMap()).thenReturn(Mono.empty());

        StepVerifier.create(endpoint.doInitialization(setupRequest(), "admin"))
                .verifyError(IllegalStateException.class);

        assertThat(SystemState.deserialize(systemStates).getIsSetup()).isFalse();
    }

    @Test
    void shouldSkipSetupWhenSetupNotRequired() {
        when(initializationStateGetter.setupRequired()).thenReturn(Mono.just(false));

        StepVerifier.create(endpoint.setup(mock(ServerRequest.class)))
                .assertNext(response -> assertThat(response.statusCode()).isEqualTo(HttpStatus.FOUND))
                .verifyComplete();

        verify(superAdminInitializer, never()).initialize(any());
        verify(themeService, never()).installPresetTheme();
    }

    @Test
    void shouldRecordSetupUsernameBeforeInitializingAdmin() {
        when(superAdminInitializer.initialize(any()))
                .thenReturn(Mono.error(new IllegalStateException("Failed to initialize super admin")));

        StepVerifier.create(endpoint.doInitialization(setupRequest(), "admin"))
                .verifyError(IllegalStateException.class);

        var systemState = SystemState.deserialize(systemStates);
        assertThat(systemState.getIsSetup()).isFalse();
        assertThat(systemState.getSetupUsername()).isEqualTo("admin");
    }

    @Test
    void shouldPreserveBasicSettingsAndPublishEffectiveExternalUrl() {
        var existingBasic = """
            {"title":"Existing","language":"zh-CN","externalUrl":"https://existing.example.com"}
            """;
        systemConfig.setData(new HashMap<>(Map.of(SystemSetting.Basic.GROUP, existingBasic)));
        var setupRequest = setupRequest();
        setupRequest.setExternalUrl("https://another.example.com");

        StepVerifier.create(endpoint.doInitialization(setupRequest, "admin")).verifyComplete();

        var basic = JsonUtils.jsonToObject(
                systemConfig.getData().get(SystemSetting.Basic.GROUP), SystemSetting.Basic.class);
        assertThat(basic.getTitle()).isEqualTo("Existing");
        assertThat(basic.getLanguage()).isEqualTo("zh-CN");
        assertThat(basic.getExternalUrl()).isEqualTo("https://existing.example.com");

        var eventCaptor = ArgumentCaptor.forClass(ExternalUrlChangedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getExternalUrl()).hasToString("https://existing.example.com");
    }

    @Test
    void shouldConvergeOnRecordedUsernameWhenResuming() {
        var interruptedState = new SystemState();
        interruptedState.setIsSetup(false);
        interruptedState.setSetupUsername("admin");
        SystemState.update(interruptedState, systemStates);
        var setupRequest = setupRequest();
        setupRequest.setUsername("another");
        var paramCaptor = ArgumentCaptor.forClass(SuperAdminInitializer.InitializationParam.class);

        StepVerifier.create(endpoint.doInitialization(setupRequest, "another")).verifyComplete();

        verify(superAdminInitializer).initialize(paramCaptor.capture());
        assertThat(paramCaptor.getValue().getUsername()).isEqualTo("admin");
        assertThat(SystemState.deserialize(systemStates).getSetupUsername()).isEqualTo("admin");
    }

    private static SystemSetupEndpoint.SetupRequest setupRequest() {
        var request = new SystemSetupEndpoint.SetupRequest();
        request.setUsername("admin");
        request.setPassword("password");
        request.setEmail("admin@example.com");
        request.setSiteTitle("Halo");
        request.setLanguage("en");
        request.setExternalUrl("http://localhost:8090");
        return request;
    }

    private static ConfigMap configMap(String name) {
        var configMap = new ConfigMap();
        configMap.setMetadata(new Metadata());
        configMap.getMetadata().setName(name);
        configMap.setData(new HashMap<>());
        return configMap;
    }
}
