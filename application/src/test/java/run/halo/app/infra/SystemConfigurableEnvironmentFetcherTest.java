package run.halo.app.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;

import java.util.LinkedHashMap;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.utils.JsonUtils;

/**
 * Tests for {@link SystemConfigurableEnvironmentFetcher}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class SystemConfigurableEnvironmentFetcherTest {

    @Mock
    private ReactiveExtensionClient client;

    @InjectMocks
    private SystemConfigurableEnvironmentFetcher environmentFetcher;

    @BeforeEach
    void setUp() {
        lenient().when(client.fetch(eq(ConfigMap.class), eq("system-default")))
            .thenReturn(Mono.just(systemDefault()));
        lenient().when(client.fetch(eq(ConfigMap.class), eq("system")))
            .thenReturn(Mono.just(system()));
    }

    @Test
    void getConfigMap() {
        environmentFetcher.getConfigMap()
            .as(StepVerifier::create)
            .consumeNextWith(configMap -> {
                assertThat(configMap.getMetadata().getName())
                    .isEqualTo(SystemSetting.SYSTEM_CONFIG);
                try {
                    JSONAssert.assertEquals(expectedJson(),
                        JsonUtils.objectToJson(configMap),
                        true);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            })
            .verifyComplete();
    }

    String expectedJson() {
        String routeRules =
            "{\\\"categories\\\":\\\"topics\\\",\\\"archives\\\":\\\"archives-new\\\","
                + "\\\"post\\\":\\\"/archives-new/{slug}\\\"}";
        String fakeArray = "{\\\"select\\\":[{\\\"label\\\":\\\"Hello\\\","
            + "\\\"value\\\":\\\"hello\\\"},{\\\"label\\\":\\\"Awesome\\\","
            + "\\\"value\\\":\\\"awesome\\\"}]}";
        return """
            {
                "data": {
                    "routeRules": "%s",
                    "seo": "{\\"blockSpiders\\":\\"true\\",\\"keywords\\":\\"Hello,Test,Fake\\"}",
                    "fakeArray": "%s"
                },
                "apiVersion": "v1alpha1",
                "kind": "ConfigMap",
                "metadata": {
                    "name": "system"
                }
            }
            """.formatted(routeRules, fakeArray);
    }

    ConfigMap systemDefault() {
        ConfigMap configMap = new ConfigMap();
        configMap.setMetadata(new Metadata());
        configMap.getMetadata().setName("system-default");
        configMap.setData(new LinkedHashMap<>());
        configMap.getData().put("routeRules", """
            {
                "categories": "categories",
                "archives": "archives",
                "post": "/archives/{slug}",
                "tags": "tags"
            }
            """
        );
        configMap.getData().put("seo", """
            {
                "blockSpiders": "false",
                "keywords": "Hello,Test,Fake"
            }
            """
        );
        configMap.getData().put("post", """
            {
                "pageSize": "10"
            }
            """
        );
        configMap.getData().put("fakeArray", """
            {
                "select": [{
                    "label": "Hello",
                    "value": "hello"
                }, {
                    "label": "Test",
                    "value": "test"
                }]
            }
            """
        );
        return configMap;
    }

    ConfigMap system() {
        ConfigMap configMap = new ConfigMap();
        configMap.setMetadata(new Metadata());
        configMap.getMetadata().setName("system");
        configMap.setData(new LinkedHashMap<>());
        // will delete the tags key and replace some values
        configMap.getData().put("routeRules", """
            {
                "categories": "topics",
                "archives": "archives-new",
                "post": "/archives-new/{slug}",
                "tags": null
            }
            """
        );
        configMap.getData().put("seo", """
            {
                "blockSpiders": "true"
            }
            """
        );

        // deleted post group here
        configMap.getData().put("post", null);

        configMap.getData().put("fakeArray", """
            {
                "select": [{
                    "label": "Hello",
                    "value": "hello"
                }, {
                    "label": "Awesome",
                    "value": "awesome"
                }]
            }
            """
        );
        return configMap;
    }
}