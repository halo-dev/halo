package run.halo.app.core.attachment.endpoint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
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
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.transaction.ReactiveTransaction;
import org.springframework.transaction.ReactiveTransactionManager;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.attachment.Policy;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Scheme;
import run.halo.app.extension.exception.ExtensionNotFoundException;

@ExtendWith(MockitoExtension.class)
class PolicyEndpointTest {

    @Mock
    ReactiveExtensionClient client;

    @Spy
    ObjectMapper mapper = JsonMapper.builder().build();

    @Mock
    ReactiveTransactionManager txManager;

    @InjectMocks
    PolicyEndpoint endpoint;

    WebTestClient webClient;

    @BeforeEach
    void setUp() {
        webClient = WebTestClient.bindToRouterFunction(endpoint.endpoint())
            .build();
    }

    @Test
    void shouldRespondNotFoundIfPolicyNotFound() {
        // Implement test logic here
        var policyScheme = Scheme.buildFromType(Policy.class);
        when(client.get(Policy.class, "fake-policy"))
            .thenReturn(Mono.error(() -> new ExtensionNotFoundException(
                policyScheme.groupVersionKind(), "fake-policy")
            ));
        webClient.get().uri("/policies/fake-policy/configs/fake-group")
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    void shouldRespondNullIfNoConfigFound() {
        when(client.get(Policy.class, "fake-policy")).thenReturn(Mono.fromSupplier(() -> {
            var policy = new Policy();
            policy.setSpec(new Policy.PolicySpec());
            policy.getSpec().setConfigMapName("fake-config-map");
            return policy;
        }));

        when(client.fetch(ConfigMap.class, "fake-config-map"))
            .thenReturn(Mono.empty());

        webClient.get().uri("/policies/fake-policy/configs/fake-group")
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class)
            .isEqualTo("null");
    }

    @Test
    void shouldRespondNullIfGroupNotFound() {
        when(client.get(Policy.class, "fake-policy")).thenReturn(Mono.fromSupplier(() -> {
            var policy = new Policy();
            policy.setSpec(new Policy.PolicySpec());
            policy.getSpec().setConfigMapName("fake-config-map");
            return policy;
        }));

        when(client.fetch(ConfigMap.class, "fake-config-map"))
            .thenReturn(Mono.fromSupplier(() -> {
                var cm = new ConfigMap();
                cm.setData(new HashMap<>());
                return cm;
            }));

        webClient.get().uri("/policies/fake-policy/configs/fake-group")
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class)
            .isEqualTo("null");
    }

    @Test
    void shouldRespondConfigIfGroupFound() {
        when(client.get(Policy.class, "fake-policy")).thenReturn(Mono.fromSupplier(() -> {
            var policy = new Policy();
            policy.setSpec(new Policy.PolicySpec());
            policy.getSpec().setConfigMapName("fake-config-map");
            return policy;
        }));

        when(client.fetch(ConfigMap.class, "fake-config-map"))
            .thenReturn(Mono.fromSupplier(() -> {
                var cm = new ConfigMap();
                cm.setData(new HashMap<>());
                cm.getData().put("fake-group", """
                    {
                      "halo": "awesome"
                    }""");
                return cm;
            }));

        webClient.get().uri("/policies/fake-policy/configs/fake-group")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.halo").isEqualTo("awesome");
    }


    @Test
    void shouldUpdateConfigIfPresent() {
        when(client.get(Policy.class, "fake-policy")).thenReturn(Mono.fromSupplier(() -> {
            var policy = new Policy();
            policy.setSpec(new Policy.PolicySpec());
            policy.getSpec().setConfigMapName("fake-config-map");
            return policy;
        }));

        var cm = new ConfigMap();
        cm.setMetadata(new Metadata());
        cm.getMetadata().setName("fake-config-map");
        cm.getMetadata().setVersion(1L);
        cm.setData(new HashMap<>());
        cm.getData().put("fake-group", """
            {
              "halo": "awesome"
            }""");
        when(client.fetch(ConfigMap.class, "fake-config-map"))
            .thenReturn(Mono.just(cm));

        var tx = mock(ReactiveTransaction.class);
        when(txManager.getReactiveTransaction(any())).thenReturn(Mono.just(tx));
        when(txManager.commit(tx)).thenReturn(Mono.empty());

        when(client.update(cm)).thenReturn(Mono.just(cm));

        var body = """
            {
              "halo": "nice",
              "key": "value"
            }""";

        webClient.put().uri("/policies/fake-policy/configs/fake-group")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus().isNoContent();

        verify(client).<ConfigMap>update(assertArg(gotCm -> {
            var data = gotCm.getData();
            JSONAssert.assertEquals(body, data.get("fake-group"), true);
        }));
    }

    @Test
    void shouldCreateConfigIfAbsent() {
        var policy = new Policy();
        policy.setSpec(new Policy.PolicySpec());
        when(client.get(Policy.class, "fake-policy")).thenReturn(Mono.just(policy));


        var tx = mock(ReactiveTransaction.class);
        when(txManager.getReactiveTransaction(any())).thenReturn(Mono.just(tx));
        when(txManager.commit(tx)).thenReturn(Mono.empty());

        var cm = new ConfigMap();
        cm.setMetadata(new Metadata());
        cm.getMetadata().setName("fake-config-map");
        cm.getMetadata().setVersion(1L);
        cm.setData(new HashMap<>());
        cm.getData().put("fake-group", """
            {
              "halo": "nice",
              "key": "value"
            }\
            """);
        when(client.create(any(ConfigMap.class))).thenReturn(Mono.just(cm));
        when(client.update(policy)).thenReturn(Mono.just(policy));

        var body = """
            {
              "halo": "nice",
              "key": "value"
            }""";

        webClient.put().uri("/policies/fake-policy/configs/fake-group")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus().isNoContent();

        verify(client).<ConfigMap>create(assertArg(gotCm -> {
            var data = gotCm.getData();
            JSONAssert.assertEquals(body, data.get("fake-group"), true);
        }));

        verify(client).<Policy>update(assertArg(
            gotPolicy -> assertEquals("fake-config-map", gotPolicy.getSpec().getConfigMapName())
        ));
    }
}
