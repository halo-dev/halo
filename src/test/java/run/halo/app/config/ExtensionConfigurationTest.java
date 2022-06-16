package run.halo.app.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;
import run.halo.app.extension.FakeExtension;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.Scheme;
import run.halo.app.extension.SchemeManager;
import run.halo.app.security.authorization.PolicyRule;
import run.halo.app.security.authorization.Role;
import run.halo.app.security.authorization.RoleGetter;

@SpringBootTest
@AutoConfigureWebTestClient
@AutoConfigureTestDatabase
class ExtensionConfigurationTest {

    @Autowired
    WebTestClient webClient;

    @Autowired
    SchemeManager schemeManager;

    @MockBean
    RoleGetter roleGetter;

    @BeforeEach
    void setUp() {
        // disable authorization
        var rule = new PolicyRule();
        rule.setApiGroups(new String[] {"*"});
        rule.setResources(new String[] {"*"});
        rule.setVerbs(new String[] {"*"});
        var role = new Role();
        role.setRules(List.of(rule));
        when(roleGetter.getRole(anyString())).thenReturn(role);
    }

    @AfterEach
    void cleanUp() {
        schemeManager.fetch(Scheme.buildFromType(FakeExtension.class).groupVersionKind())
            .ifPresent(schemeManager::unregister);
    }

    @Test
    @WithMockUser
    void shouldReturnNotFoundWhenSchemeNotRegistered() {
        webClient.get()
            .uri("/apis/fake.halo.run/v1alpha1/fakes")
            .exchange()
            .expectStatus().isNotFound();

        webClient.get()
            .uri("/apis/fake.halo.run/v1alpha1/fakes/my-fake")
            .exchange()
            .expectStatus().isNotFound();

        webClient.post()
            .uri("/apis/fake.halo.run/v1alpha1/fakes")
            .bodyValue(new FakeExtension())
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    @WithMockUser
    void shouldListExtensionsWhenSchemeRegistered() {
        schemeManager.register(FakeExtension.class);

        webClient.get()
            .uri("/apis/fake.halo.run/v1alpha1/fakes")
            .exchange()
            .expectStatus().isOk();
    }

    @Test
    @WithMockUser
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldCreateExtensionWhenSchemeRegistered() {
        schemeManager.register(FakeExtension.class);

        getCreateExtensionResponse()
            .expectStatus().isOk()
            .expectBody(FakeExtension.class)
            .consumeWith(result -> {
                var gotFake = result.getResponseBody();
                assertNotNull(gotFake);
                assertEquals("my-fake", gotFake.getMetadata().getName());
                assertNotNull(gotFake.getMetadata().getVersion());
                assertNotNull(gotFake.getMetadata().getCreationTimestamp());
            });
    }

    @Test
    @WithMockUser
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldGetExtensionWhenSchemeRegistered() {
        schemeManager.register(FakeExtension.class);

        // create the Extension
        getCreateExtensionResponse().expectStatus().isOk();

        webClient.get()
            .uri("/apis/fake.halo.run/v1alpha1/fakes/my-fake")
            .exchange()
            .expectStatus().isOk()
            .expectBody(FakeExtension.class)
            .consumeWith(result -> {
                var gotFake = result.getResponseBody();
                assertNotNull(gotFake);
                assertEquals("my-fake", gotFake.getMetadata().getName());
                assertNotNull(gotFake.getMetadata().getVersion());
                assertNotNull(gotFake.getMetadata().getCreationTimestamp());
            });
    }

    WebTestClient.ResponseSpec getCreateExtensionResponse() {
        var metadata = new Metadata();
        metadata.setName("my-fake");
        var fake = new FakeExtension();
        fake.setMetadata(metadata);

        return webClient.post()
            .uri("/apis/fake.halo.run/v1alpha1/fakes")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(fake)
            .exchange();
    }

}