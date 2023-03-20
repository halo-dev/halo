package run.halo.app.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.service.RoleService;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.FakeExtension;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.Scheme;
import run.halo.app.extension.SchemeManager;
import run.halo.app.extension.store.ExtensionStoreRepository;

@SpringBootTest
@AutoConfigureWebTestClient
class ExtensionConfigurationTest {

    @Autowired
    WebTestClient webClient;

    @Autowired
    SchemeManager schemeManager;

    @MockBean
    RoleService roleService;

    @BeforeEach
    void setUp() {
        // disable authorization
        var rule = new Role.PolicyRule.Builder()
            .apiGroups("*")
            .resources("*")
            .verbs("*")
            .build();
        var role = new Role();
        role.setMetadata(new Metadata());
        role.getMetadata().setName("supper-role");
        role.setRules(List.of(rule));
        when(roleService.getMonoRole(anyString())).thenReturn(Mono.just(role));
        when(roleService.listDependenciesFlux(anySet())).thenReturn(Flux.just(role));
        // register scheme
        schemeManager.register(FakeExtension.class);

        webClient = webClient.mutateWith(csrf());
    }

    @AfterEach
    void cleanUp(@Autowired ExtensionStoreRepository repository) {
        repository.deleteAll().subscribe();
    }

    @Test
    @WithMockUser
    void shouldReturnNotFoundWhenSchemeNotRegistered() {
        // unregister the Extension if necessary
        schemeManager.fetch(Scheme.buildFromType(FakeExtension.class).groupVersionKind())
            .ifPresent(schemeManager::unregister);

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

        webClient.put()
            .uri("/apis/fake.halo.run/v1alpha1/fakes/my-fake")
            .bodyValue(new FakeExtension())
            .exchange()
            .expectStatus().isNotFound();

        webClient.delete()
            .uri("/apis/fake.halo.run/v1alpha1/fakes/my-fake")
            .exchange()
            .expectStatus().isNotFound();
    }

    @Nested
    @DisplayName("After creating extension")
    class AfterCreatingExtension {

        @Autowired
        ExtensionClient extClient;

        FakeExtension createdFake;

        @BeforeEach
        void setUp() {

            var metadata = new Metadata();
            metadata.setName("my-fake");
            metadata.setLabels(Map.of("label-key", "label-value"));
            var fake = new FakeExtension();
            fake.setMetadata(metadata);

            createdFake = webClient.post()
                .uri("/apis/fake.halo.run/v1alpha1/fakes")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(fake)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().location("/apis/fake.halo.run/v1alpha1/fakes/my-fake")
                .expectBody(FakeExtension.class)
                .consumeWith(result -> {
                    var gotFake = result.getResponseBody();
                    assertNotNull(gotFake);
                    assertEquals("my-fake", gotFake.getMetadata().getName());
                    assertNotNull(gotFake.getMetadata().getVersion());
                    assertNotNull(gotFake.getMetadata().getCreationTimestamp());
                })
                .returnResult()
                .getResponseBody();
        }

        @Test
        @WithMockUser
        void shouldDeleteExtensionWhenSchemeRegistered() {
            webClient.delete()
                .uri("/apis/fake.halo.run/v1alpha1/fakes/{name}",
                    createdFake.getMetadata().getName())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(FakeExtension.class)
                .consumeWith(result -> {
                    var deletedFake = result.getResponseBody();
                    assertNotNull(deletedFake);
                    assertNotNull(deletedFake.getMetadata().getDeletionTimestamp());
                    assertTrue(deletedFake.getMetadata().getDeletionTimestamp()
                        .isBefore(Instant.now()));
                });
        }

        @Test
        @WithMockUser
        void shouldListExtensionsWhenSchemeRegistered() {
            webClient.get().uri("/apis/fake.halo.run/v1alpha1/fakes")
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.items.length()").isEqualTo(1);
        }

        @Test
        @WithMockUser
        void shouldListExtensionsWithMatchedSelectors() {
            webClient.get().uri(uriBuilder -> uriBuilder
                    .path("/apis/fake.halo.run/v1alpha1/fakes")
                    .queryParam("labelSelector", "label-key=label-value")
                    .queryParam("fieldSelector", "name=my-fake")
                    .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.items.length()").isEqualTo(1);
        }

        @Test
        @WithMockUser
        void shouldListExtensionsWithMismatchedSelectors() {
            webClient.get().uri(uriBuilder -> uriBuilder
                    .path("/apis/fake.halo.run/v1alpha1/fakes")
                    .queryParam("labelSelector", "label-key=invalid-label-value")
                    .queryParam("fieldSelector", "name=invalid-name")
                    .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.items.length()").isEqualTo(0);
        }

        @Test
        @WithMockUser
        void shouldUpdateExtensionWhenSchemeRegistered() {
            var name = createdFake.getMetadata().getName();
            FakeExtension fakeToUpdate = getFakeExtension(name);
            fakeToUpdate.getMetadata().setLabels(Map.of("updated", "true"));

            webClient.put()
                .uri("/apis/fake.halo.run/v1alpha1/fakes/{name}", name)
                .bodyValue(fakeToUpdate)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(FakeExtension.class)
                .consumeWith(result -> {
                    var updatedFake = result.getResponseBody();
                    assertNotNull(updatedFake);
                    assertNotEquals(fakeToUpdate.getMetadata().getVersion(),
                        updatedFake.getMetadata().getVersion());
                    assertEquals(Map.of("updated", "true"),
                        updatedFake.getMetadata().getLabels());
                });
        }

        @Test
        @WithMockUser
        void shouldGetExtensionWhenSchemeRegistered() {
            var name = createdFake.getMetadata().getName();
            webClient.get()
                .uri("/apis/fake.halo.run/v1alpha1/fakes/{name}", name)
                .exchange()
                .expectStatus().isOk()
                .expectBody(FakeExtension.class)
                .consumeWith(result -> {
                    var gotFake = result.getResponseBody();
                    assertNotNull(gotFake);
                    assertEquals(name, gotFake.getMetadata().getName());
                    assertNotNull(gotFake.getMetadata().getVersion());
                    assertNotNull(gotFake.getMetadata().getCreationTimestamp());
                });
        }

        FakeExtension getFakeExtension(String name) {
            return webClient.get()
                .uri("/apis/fake.halo.run/v1alpha1/fakes/{name}", name)
                .exchange()
                .expectStatus().isOk()
                .expectBody(FakeExtension.class)
                .returnResult()
                .getResponseBody();
        }

    }

}