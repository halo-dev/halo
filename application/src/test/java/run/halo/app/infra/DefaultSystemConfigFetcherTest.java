package run.halo.app.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.ConversionService;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ReactiveExtensionClient;

/**
 * Tests for {@link DefaultSystemConfigFetcher}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class DefaultSystemConfigFetcherTest {

    @Mock
    ReactiveExtensionClient client;

    @Mock
    ConversionService conversionService;

    @InjectMocks
    DefaultSystemConfigFetcher systemConfigFetcher;

    private ConfigMap mockConfigMap;

    @BeforeEach
    void setUp() {
        mockConfigMap = new ConfigMap();
        mockConfigMap.setData(Map.of(
            "basic", """
                {
                  "title": "Test Blog",
                  "subtitle": "Test"
                }""",
            "post", """
                {
                  "postPageSize": 10
                }"""
        ));
    }

    @Test
    void testFetchWithConvertibleType() {
        // Arrange
        when(conversionService.canConvert(String.class, String.class))
            .thenReturn(true);
        when(conversionService.convert("testValue", String.class))
            .thenReturn("testValue");

        var configMapWithString = new ConfigMap();
        var data = new HashMap<String, String>();
        data.put("testKey", "testValue");
        configMapWithString.setData(data);

        systemConfigFetcher.getConfigMapCache().set(configMapWithString.getData());

        // Act & Assert
        systemConfigFetcher.fetch("testKey", String.class)
            .as(StepVerifier::create)
            .expectNext("testValue")
            .verifyComplete();
    }

    @Test
    void testFetchWithJsonConversion() {
        // Arrange
        var configMap = new ConfigMap();
        configMap.setData(Map.of(
            "basic", """
                {
                  "title": "Test Blog",
                  "subtitle": "Test Subtitle"
                }"""
        ));
        systemConfigFetcher.getConfigMapCache().set(configMap.getData());

        when(conversionService.canConvert(String.class, SystemSetting.Basic.class))
            .thenReturn(false);

        // Act & Assert
        systemConfigFetcher.fetch("basic", SystemSetting.Basic.class)
            .as(StepVerifier::create)
            .assertNext(basic -> {
                assertThat(basic.getTitle()).isEqualTo("Test Blog");
                assertThat(basic.getSubtitle()).isEqualTo("Test Subtitle");
            })
            .verifyComplete();
    }

    @Test
    void testFetchWhenKeyDoesNotExist() {
        // Arrange
        systemConfigFetcher.getConfigMapCache().set(mockConfigMap.getData());

        // Act & Assert
        systemConfigFetcher.fetch("nonExistentKey", String.class)
            .as(StepVerifier::create)
            .verifyComplete();
    }

    @Test
    void testGetBasicWithValidData() {
        // Arrange
        var configMap = new ConfigMap();
        configMap.setData(Map.of(
            "basic", """
                {
                  "title": "My Blog",
                  "subtitle": "My Subtitle",
                  "logo": "logo.png"
                }"""
        ));
        systemConfigFetcher.getConfigMapCache().set(configMap.getData());

        when(conversionService.canConvert(String.class, SystemSetting.Basic.class))
            .thenReturn(false);

        // Act & Assert
        systemConfigFetcher.getBasic()
            .as(StepVerifier::create)
            .assertNext(basic -> {
                assertThat(basic.getTitle()).isEqualTo("My Blog");
                assertThat(basic.getSubtitle()).isEqualTo("My Subtitle");
                assertThat(basic.getLogo()).isEqualTo("logo.png");
            })
            .verifyComplete();
    }

    @Test
    void testGetBasicWhenKeyDoesNotExist() {
        // Arrange
        systemConfigFetcher.getConfigMapCache().set(Map.of());

        // Act & Assert - should return a new instance
        systemConfigFetcher.getBasic()
            .as(StepVerifier::create)
            .assertNext(basic -> assertThat(basic).isNotNull())
            .verifyComplete();
    }

    @Test
    void testFetchComment() {
        // Arrange
        var configMap = new ConfigMap();
        configMap.setData(Map.of(
            "comment", """
                {
                  "enable": true
                }"""
        ));
        systemConfigFetcher.getConfigMapCache().set(configMap.getData());

        when(conversionService.canConvert(String.class, SystemSetting.Comment.class))
            .thenReturn(false);

        // Act & Assert
        systemConfigFetcher.fetchComment()
            .as(StepVerifier::create)
            .expectNextMatches(java.util.Objects::nonNull)
            .verifyComplete();
    }

    @Test
    void testFetchPost() {
        // Arrange
        var configMap = new ConfigMap();
        configMap.setData(Map.of(
            "post", """
                {
                  "postPageSize": 10,
                  "archivePageSize": 20
                }"""
        ));
        systemConfigFetcher.getConfigMapCache().set(configMap.getData());

        when(conversionService.canConvert(String.class, SystemSetting.Post.class))
            .thenReturn(false);

        // Act & Assert
        systemConfigFetcher.fetchPost()
            .as(StepVerifier::create)
            .assertNext(post -> {
                assertThat(post.getPostPageSize()).isEqualTo(10);
                assertThat(post.getArchivePageSize()).isEqualTo(20);
            })
            .verifyComplete();
    }

    @Test
    void testFetchRouteRules() {
        // Arrange
        var configMap = new ConfigMap();
        configMap.setData(Map.of(
            "routeRules", """
                {
                  "post": "/articles/{slug}",
                  "tags": "/labels"
                }"""
        ));
        systemConfigFetcher.getConfigMapCache().set(configMap.getData());

        when(conversionService.canConvert(String.class, SystemSetting.ThemeRouteRules.class))
            .thenReturn(false);

        // Act & Assert
        systemConfigFetcher.fetchRouteRules()
            .as(StepVerifier::create)
            .assertNext(rules -> {
                assertThat(rules.getPost()).isEqualTo("/articles/{slug}");
                assertThat(rules.getTags()).isEqualTo("/labels");
            })
            .verifyComplete();
    }

    @Test
    void testGetConfig() {
        // Arrange
        var configData = mockConfigMap.getData();
        systemConfigFetcher.getConfigMapCache().set(configData);

        // Act & Assert
        systemConfigFetcher.getConfig()
            .as(StepVerifier::create)
            .expectNext(java.util.Objects.requireNonNull(configData))
            .verifyComplete();
    }

    @Test
    void testGetConfigMap() {
        // Arrange
        when(client.fetch(ConfigMap.class, "system"))
            .thenReturn(Mono.just(mockConfigMap));

        // Act & Assert
        systemConfigFetcher.getConfigMap()
            .as(StepVerifier::create)
            .expectNext(mockConfigMap)
            .verifyComplete();
    }

    @Test
    void testGetConfigMapBlocking() {
        // Arrange
        when(client.fetch(ConfigMap.class, "system"))
            .thenReturn(Mono.just(mockConfigMap));

        // Act
        Optional<ConfigMap> result = systemConfigFetcher.getConfigMapBlocking();

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(mockConfigMap);
    }

    @Test
    void testGetConfigMapBlockingWhenNotFound() {
        // Arrange
        when(client.fetch(ConfigMap.class, "system"))
            .thenReturn(Mono.empty());

        // Act
        Optional<ConfigMap> result = systemConfigFetcher.getConfigMapBlocking();

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void testOnApplicationEvent() {
        // Arrange
        var oldData = new HashMap<String, String>();
        var newDataMap = new HashMap<String, String>();
        newDataMap.put("key1", "value1");
        newDataMap.put("key2", "value2");
        var event = new SystemConfigChangedEvent(this, oldData, newDataMap);

        // Act
        systemConfigFetcher.onApplicationEvent(event);

        // Assert
        assertThat(systemConfigFetcher.getConfigMapCache().get()).isEqualTo(newDataMap);
    }

    @Test
    void testCacheInvalidation() {
        // Arrange
        var initialDataMap = new HashMap<String, String>();
        initialDataMap.put("key1", "value1");
        systemConfigFetcher.getConfigMapCache().set(initialDataMap);

        // Act & Assert - Verify cache is updated when new data differs
        var newDataMap = new HashMap<String, String>();
        newDataMap.put("key1", "newValue");
        var event = new SystemConfigChangedEvent(this, initialDataMap, newDataMap);
        systemConfigFetcher.onApplicationEvent(event);

        assertThat(systemConfigFetcher.getConfigMapCache().get()).isEqualTo(newDataMap);
    }

    @Test
    void shouldGetConfigMapFromDatabaseIfNoCache() {
        // Arrange
        when(client.fetch(ConfigMap.class, SystemSetting.SYSTEM_CONFIG))
            .thenReturn(Mono.just(mockConfigMap));
        when(client.fetch(ConfigMap.class, SystemSetting.SYSTEM_CONFIG_DEFAULT))
            .thenReturn(Mono.empty());

        assertNotNull(mockConfigMap.getData());
        systemConfigFetcher.getConfigMapMono()
            .as(StepVerifier::create)
            .expectNext(mockConfigMap.getData())
            .verifyComplete();
    }

    @Test
    void shouldGetConfigMapFromCacheIfPresent() {
        // Arrange
        var cachedData = Map.of(
            "cachedKey", """
                {
                  "key1": "value1"
                }"""
        );
        systemConfigFetcher.getConfigMapCache().set(cachedData);

        // Act & Assert
        systemConfigFetcher.getConfigMapMono()
            .as(StepVerifier::create)
            .expectNext(cachedData)
            .verifyComplete();

        verify(client, never()).fetch(ConfigMap.class, SystemSetting.SYSTEM_CONFIG);
    }
}