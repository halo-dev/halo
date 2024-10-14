package run.halo.app.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Sort;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.AuthProvider;
import run.halo.app.core.extension.UserConnection;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
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
    ReactiveExtensionClient client;

    @Mock
    ObjectProvider<SystemConfigurableEnvironmentFetcher> systemFetchProvider;

    @Mock
    SystemConfigurableEnvironmentFetcher systemConfigFetcher;

    @InjectMocks
    AuthProviderServiceImpl authProviderService;

    @BeforeEach
    void setUp() {
        when(systemFetchProvider.getIfUnique()).thenReturn(systemConfigFetcher);
    }

    @Test
    void testEnable() throws JSONException {
        // Create a test auth provider
        AuthProvider authProvider = createAuthProvider("github");
        when(client.get(eq(AuthProvider.class), eq("github"))).thenReturn(Mono.just(authProvider));

        ArgumentCaptor<ConfigMap> captor = ArgumentCaptor.forClass(ConfigMap.class);
        when(client.update(captor.capture())).thenReturn(Mono.empty());

        pileSystemConfigMap();

        // Call the method being tested
        authProviderService.enable("github")
            .as(StepVerifier::create)
            .expectNext(authProvider)
            .verifyComplete();

        ConfigMap value = captor.getValue();
        JSONAssert.assertEquals("""
                {
                    "enabled":["github"],
                    "states": [
                        {
                            "name": "github",
                            "enabled": true,
                            "priority": 0
                        }
                    ]
                }
                """,
            value.getData().get(SystemSetting.AuthProvider.GROUP),
            true);
        // Verify the result
        verify(client).get(AuthProvider.class, "github");
    }

    @Test
    void testDisable() throws JSONException {
        // Create a test auth provider
        AuthProvider authProvider = createAuthProvider("github");
        when(client.get(eq(AuthProvider.class), eq("github"))).thenReturn(Mono.just(authProvider));

        AuthProvider local = createAuthProvider("local");
        local.getMetadata().getLabels().put(AuthProvider.PRIVILEGED_LABEL, "true");

        ArgumentCaptor<ConfigMap> captor = ArgumentCaptor.forClass(ConfigMap.class);
        when(client.update(captor.capture())).thenReturn(Mono.empty());

        pileSystemConfigMap();

        // Call the method being tested
        Mono<AuthProvider> result = authProviderService.disable("github");

        assertEquals(authProvider, result.block());
        ConfigMap value = captor.getValue();
        JSONAssert.assertEquals("""
                {
                    "enabled":[],
                    "states": [
                        {
                            "name": "github",
                            "enabled": false,
                            "priority": 0
                        }
                    ]
                }
                """,
            value.getData().get(SystemSetting.AuthProvider.GROUP),
            true);
        // Verify the result
        verify(client).get(AuthProvider.class, "github");
    }

    @Test
    @WithMockUser(username = "admin")
    void listAll() {
        AuthProvider github = createAuthProvider("github");
        github.getSpec().setBindingUrl("fake-binding-url");

        AuthProvider gitlab = createAuthProvider("gitlab");
        gitlab.getSpec().setBindingUrl("fake-binding-url");

        AuthProvider gitee = createAuthProvider("gitee");

        when(client.listAll(same(AuthProvider.class), any(ListOptions.class), any(Sort.class)))
            .thenReturn(Flux.just(github, gitlab, gitee));
        when(client.listAll(same(UserConnection.class), any(ListOptions.class), any(Sort.class)))
            .thenReturn(Flux.empty());

        pileSystemConfigMap();

        authProviderService.listAll()
            .as(StepVerifier::create)
            .consumeNextWith(result -> {
                assertThat(result).hasSize(3);
                try {
                    JSONAssert.assertEquals("""
                            [{
                                 "name": "gitee",
                                 "displayName": "gitee",
                                 "authType": "OAUTH2",
                                 "isBound": false,
                                 "enabled": false,
                                 "priority": 0,
                                 "supportsBinding": false,
                                 "privileged": false
                             },
                             {
                                 "name": "github",
                                 "displayName": "github",
                                 "bindingUrl": "fake-binding-url",
                                 "authType": "OAUTH2",
                                 "isBound": false,
                                 "enabled": false,
                                 "priority": 0,
                                 "supportsBinding": false,
                                 "privileged": false
                             },
                             {
                                 "name": "gitlab",
                                 "displayName": "gitlab",
                                 "bindingUrl": "fake-binding-url",
                                 "authType": "OAUTH2",
                                 "isBound": false,
                                 "enabled": false,
                                 "priority": 0,
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
        authProvider.getSpec().setAuthType(AuthProvider.AuthType.OAUTH2);
        return authProvider;
    }

    void pileSystemConfigMap() {
        ConfigMap configMap = new ConfigMap();
        configMap.setData(new HashMap<>());
        when(systemConfigFetcher.getConfigMap())
            .thenReturn(Mono.just(configMap));
    }
}