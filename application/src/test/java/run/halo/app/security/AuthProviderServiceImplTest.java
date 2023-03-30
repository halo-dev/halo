package run.halo.app.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Set;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.AuthProvider;
import run.halo.app.core.extension.UserConnection;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.utils.JsonUtils;

/**
 * Tests for {@link AuthProviderServiceImpl}.
 *
 * @author guqing
 * @since 2.4.0
 */
@ExtendWith(SpringExtension.class)
class AuthProviderServiceImplTest {
    @Mock
    private ReactiveExtensionClient client;

    @InjectMocks
    private AuthProviderServiceImpl authProviderService;

    @Test
    void testEnable() {
        // Create a test auth provider
        AuthProvider authProvider = createAuthProvider("github");
        when(client.get(eq(AuthProvider.class), eq("github"))).thenReturn(Mono.just(authProvider));

        ArgumentCaptor<ConfigMap> captor = ArgumentCaptor.forClass(ConfigMap.class);
        when(client.update(captor.capture())).thenReturn(Mono.empty());

        ConfigMap configMap = new ConfigMap();
        configMap.setData(new HashMap<>());
        when(client.fetch(eq(ConfigMap.class), eq(SystemSetting.SYSTEM_CONFIG)))
            .thenReturn(Mono.just(configMap));

        AuthProvider local = createAuthProvider("local");
        local.getMetadata().getLabels().put(AuthProvider.PRIVILEGED_LABEL, "true");
        when(client.list(eq(AuthProvider.class), any(), any())).thenReturn(Flux.just(local));

        // Call the method being tested
        Mono<AuthProvider> result = authProviderService.enable("github");

        assertEquals(authProvider, result.block());
        ConfigMap value = captor.getValue();
        String providerSettingStr = value.getData().get(SystemSetting.AuthProvider.GROUP);
        Set<String> enabled =
            JsonUtils.jsonToObject(providerSettingStr, SystemSetting.AuthProvider.class)
                .getEnabled();
        assertThat(enabled).containsExactly("github");
        // Verify the result
        verify(client).get(AuthProvider.class, "github");
        verify(client).fetch(eq(ConfigMap.class), eq(SystemSetting.SYSTEM_CONFIG));
    }

    @Test
    void testDisable() {
        // Create a test auth provider
        AuthProvider authProvider = createAuthProvider("github");
        when(client.get(eq(AuthProvider.class), eq("github"))).thenReturn(Mono.just(authProvider));

        AuthProvider local = createAuthProvider("local");
        local.getMetadata().getLabels().put(AuthProvider.PRIVILEGED_LABEL, "true");
        when(client.list(eq(AuthProvider.class), any(), any())).thenReturn(Flux.just(local));

        ArgumentCaptor<ConfigMap> captor = ArgumentCaptor.forClass(ConfigMap.class);
        when(client.update(captor.capture())).thenReturn(Mono.empty());

        ConfigMap configMap = new ConfigMap();
        configMap.setData(new HashMap<>());
        configMap.getData().put(SystemSetting.AuthProvider.GROUP, "{\"enabled\":[\"github\"]}");
        when(client.fetch(eq(ConfigMap.class), eq(SystemSetting.SYSTEM_CONFIG)))
            .thenReturn(Mono.just(configMap));

        // Call the method being tested
        Mono<AuthProvider> result = authProviderService.disable("github");

        assertEquals(authProvider, result.block());
        ConfigMap value = captor.getValue();
        String providerSettingStr = value.getData().get(SystemSetting.AuthProvider.GROUP);
        Set<String> enabled =
            JsonUtils.jsonToObject(providerSettingStr, SystemSetting.AuthProvider.class)
                .getEnabled();
        assertThat(enabled).isEmpty();
        // Verify the result
        verify(client).get(AuthProvider.class, "github");
        verify(client).fetch(eq(ConfigMap.class), eq(SystemSetting.SYSTEM_CONFIG));
    }


    @Test
    @WithMockUser(username = "admin")
    void listAll() {
        AuthProvider github = createAuthProvider("github");
        github.getSpec().setBindingUrl("fake-binding-url");

        AuthProvider gitlab = createAuthProvider("gitlab");
        gitlab.getSpec().setBindingUrl("fake-binding-url");

        AuthProvider gitee = createAuthProvider("gitee");

        when(client.list(eq(AuthProvider.class), any(), any()))
            .thenReturn(Flux.just(github, gitlab, gitee));
        when(client.list(eq(UserConnection.class), any(), any())).thenReturn(Flux.empty());

        ConfigMap configMap = new ConfigMap();
        configMap.setData(new HashMap<>());
        configMap.getData().put(SystemSetting.AuthProvider.GROUP, "{\"enabled\":[\"github\"]}");
        when(client.fetch(eq(ConfigMap.class), eq(SystemSetting.SYSTEM_CONFIG)))
            .thenReturn(Mono.just(configMap));

        authProviderService.listAll()
            .as(StepVerifier::create)
            .consumeNextWith(result -> {
                assertThat(result).hasSize(3);
                try {
                    JSONAssert.assertEquals("""
                            [{
                                "name": "github",
                                "displayName": "github",
                                "bindingUrl": "fake-binding-url",
                                "enabled": true,
                                "isBound": false,
                                "supportsBinding": false,
                                "privileged": false
                            }, {
                                "name": "gitlab",
                                "displayName": "gitlab",
                                "bindingUrl": "fake-binding-url",
                                "enabled": false,
                                "isBound": false,
                                "supportsBinding": false,
                                "privileged": false
                            },{
                                                        
                                "name": "gitee",
                                "displayName": "gitee",
                                "enabled": false,
                                "isBound": false,
                                "supportsBinding": false,
                                "privileged": false
                            }]
                            """,
                        JsonUtils.objectToJson(result),
                        true);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            })
            .verifyComplete();
    }

    AuthProvider createAuthProvider(String name) {
        AuthProvider authProvider = new AuthProvider();
        authProvider.setMetadata(new Metadata());
        authProvider.getMetadata().setName(name);
        authProvider.getMetadata().setLabels(new HashMap<>());
        authProvider.setSpec(new AuthProvider.AuthProviderSpec());
        authProvider.getSpec().setDisplayName(name);
        return authProvider;
    }
}