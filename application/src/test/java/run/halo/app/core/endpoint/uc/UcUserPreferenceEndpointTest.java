package run.halo.app.core.endpoint.uc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.core.authority.AuthorityUtils.createAuthorityList;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockAuthentication;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.HashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;

@ExtendWith(MockitoExtension.class)
class UcUserPreferenceEndpointTest {

    WebTestClient webClient;

    @InjectMocks
    UcUserPreferenceEndpoint endpoint;

    @Mock
    ReactiveExtensionClient client;

    @Spy
    ObjectMapper mapper = JsonMapper.builder().build();

    @BeforeEach
    void setUp() {
        webClient = WebTestClient.bindToRouterFunction(endpoint.endpoint())
            .apply(springSecurity())
            .build();
    }

    @Test
    void testGroupVersion() {
        var gv = endpoint.groupVersion();
        assertEquals("uc.api.halo.run", gv.group());
        assertEquals("v1alpha1", gv.version());
    }

    @Test
    void shouldNotGetPreferenceWhenUnauthenticated() {
        webClient.mutate()
            .apply(mockAuthentication(new AnonymousAuthenticationToken(
                "key", "anonymousUser", createAuthorityList("ROLE_ANONYMOUS")
            )))
            .build()
            .get()
            .uri("/user-preferences/fake")
            .exchange()
            .expectStatus()
            .isForbidden();
    }

    @Test
    void shouldGetNullPreferenceWhenAuthenticatedAndConfigMapAbsent() {
        when(client.fetch(ConfigMap.class, "user-preferences-faker")).thenReturn(Mono.empty());
        webClient.mutate()
            .apply(mockUser("faker"))
            .build()
            .get()
            .uri("/user-preferences/fake")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(JsonNode.class)
            .isEqualTo(NullNode.getInstance());
    }

    @Test
    void shouldGetPreferenceWhenAuthenticatedAndConfigMapPresent() {
        var cm = new ConfigMap();
        cm.setData(new HashMap<>());
        cm.getData().put("fake", """
            {
              "key": "value"
            }\
            """);
        when(client.fetch(ConfigMap.class, "user-preferences-faker"))
            .thenReturn(Mono.just(cm));
        webClient.mutate()
            .apply(mockUser("faker"))
            .build()
            .get()
            .uri("/user-preferences/fake")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(ObjectNode.class)
            .value(node -> assertEquals("value", node.get("key").asText()));
    }

    @Test
    void shouldNotCreatePreferenceWhenUnauthenticated() {
        webClient.mutate()
            .apply(mockAuthentication(new AnonymousAuthenticationToken(
                "key", "anonymousUser", createAuthorityList("ROLE_ANONYMOUS")
            )))
            .build()
            .put()
            .uri("/user-preferences/faker")
            .exchange()
            .expectStatus()
            .isForbidden();
    }

    @Test
    void shouldCreatePreferenceWithoutConfigMap() {
        when(client.fetch(ConfigMap.class, "user-preferences-faker")).thenReturn(Mono.empty());
        when(client.create(any(ConfigMap.class))).thenReturn(Mono.just(new ConfigMap()));
        webClient.mutate()
            .apply(mockUser("faker"))
            .build()
            .put()
            .uri("/user-preferences/fake")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "key": "value"
                }\
                """)
            .exchange()
            .expectStatus()
            .isNoContent();

        verify(client).<ConfigMap>create(assertArg(cm -> JSONAssert.assertEquals(
            """
                {"key":"value"}\
                """,
            cm.getData().get("fake"),
            true
        )));
        verify(client, never()).update(any());
    }

    @Test
    void shouldUpdatePreferenceWhenConfigMapExists() {
        var cm = new ConfigMap();
        cm.setMetadata(new Metadata());
        cm.getMetadata().setName("user-preferences-faker");
        cm.getMetadata().setVersion(1L);
        cm.setData(new HashMap<>());
        cm.getData().put("fake1", """
            {
              "key1": "value1"
            }\
            """);
        cm.getData().put("fake2", """
            {
              "key2": "value2"
            }
            """);
        when(client.fetch(ConfigMap.class, "user-preferences-faker")).thenReturn(Mono.just(cm));
        when(client.update(any(ConfigMap.class))).thenReturn(Mono.just(cm));
        webClient.mutate()
            .apply(mockUser("faker"))
            .build()
            .put()
            .uri("/user-preferences/fake1")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "newKey": "newValue"
                }\
                """)
            .exchange()
            .expectStatus()
            .isNoContent();

        verify(client).<ConfigMap>update(assertArg(cmToUpdate -> {
            JSONAssert.assertEquals(
                """
                    {"newKey":"newValue"}\
                    """,
                cmToUpdate.getData().get("fake1"),
                true
            );
            JSONAssert.assertEquals(
                """
                    {"key2":"value2"}\
                    """,
                cmToUpdate.getData().get("fake2"),
                true
            );
        }));

        verify(client, never()).create(any());
    }

    @Test
    void shouldNotUpdatePreferenceWhenNotChange() {
        var cm = new ConfigMap();
        cm.setMetadata(new Metadata());
        cm.getMetadata().setName("user-preferences-faker");
        cm.getMetadata().setVersion(1L);
        cm.setData(new HashMap<>());
        cm.getData().put("fake", """
            {"key":"value"}\
            """);
        when(client.fetch(ConfigMap.class, "user-preferences-faker")).thenReturn(Mono.just(cm));
        webClient.mutate()
            .apply(mockUser("faker"))
            .build()
            .put()
            .uri("/user-preferences/fake")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {"key":"value"}\
                """)
            .exchange()
            .expectStatus()
            .isNoContent();

        verify(client, never()).update(any(ConfigMap.class));
        verify(client, never()).create(any(ConfigMap.class));
    }
}
