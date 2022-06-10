package run.halo.app.plugin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pf4j.PluginState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import run.halo.app.extension.Metadata;
import run.halo.app.security.authorization.DefaultRoleGetter;
import run.halo.app.security.authorization.PolicyRule;
import run.halo.app.security.authorization.Role;

/**
 * @author guqing
 * @since 2.0.0
 */
@SpringBootTest
@WithMockUser(username = "user")
@AutoConfigureWebTestClient
class PluginLifeCycleManagerControllerTest {

    @Autowired
    WebTestClient webClient;
    @MockBean
    DefaultRoleGetter defaultRoleGetter;

    @MockBean
    HaloPluginManager haloPluginManager;

    String prefix = "/apis/plugin.halo.run/v1alpha1/plugins";

    @BeforeEach
    void setUp() {
        // There is no default role available in the system, so it needs to be created
        Role role = new Role();
        role.setApiVersion("v1alpha1");
        role.setKind("Role");
        Metadata metadata = new Metadata();
        metadata.setName("test-plugin-lifecycle-role");
        role.setMetadata(metadata);
        PolicyRule policyRule = new PolicyRule.Builder()
            .apiGroups("plugin.halo.run")
            .resources("plugins", "plugins/startup", "plugins/stop")
            .verbs("*")
            .build();
        role.setRules(List.of(policyRule));
        when(defaultRoleGetter.getRole("USER")).thenReturn(role);
        when(haloPluginManager.startPlugin(any())).thenReturn(PluginState.STARTED);
        when(haloPluginManager.stopPlugin(any())).thenReturn(PluginState.STOPPED);
    }

    @Test
    void list() {
        webClient.get()
            .uri(prefix)
            .exchange()
            .expectStatus()
            .isOk();
    }

    @Test
    void start() {
        webClient.get()
            .uri(prefix + "/apples/startup")
            .exchange()
            .expectStatus()
            .isOk();
    }

    @Test
    void stop() {
        webClient.get()
            .uri(prefix + "/apples/stop")
            .exchange()
            .expectStatus()
            .isOk();
    }
}