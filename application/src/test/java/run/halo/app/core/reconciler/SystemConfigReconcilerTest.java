package run.halo.app.core.reconciler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import run.halo.app.core.extension.content.Constant;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.infra.SystemConfigChangedEvent;
import run.halo.app.infra.SystemSetting;

@ExtendWith(MockitoExtension.class)
class SystemConfigReconcilerTest {

    @Mock
    ExtensionClient client;

    @Mock
    ApplicationEventPublisher eventPublisher;

    @InjectMocks
    SystemConfigReconciler reconciler;

    ConfigMap systemConfigMap;
    ConfigMap defaultConfigMap;

    @BeforeEach
    void setUp() {
        systemConfigMap = createConfigMap(SystemSetting.SYSTEM_CONFIG);
        defaultConfigMap = createConfigMap(SystemSetting.SYSTEM_CONFIG_DEFAULT);
    }

    @Test
    void reconcileShouldDoNothingWhenConfigMapNotFound() {
        var request = new Reconciler.Request(SystemSetting.SYSTEM_CONFIG);
        when(client.fetch(ConfigMap.class, SystemSetting.SYSTEM_CONFIG))
            .thenReturn(Optional.empty());

        reconciler.reconcile(request);

        verify(client, never()).update(any(ConfigMap.class));
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void reconcileShouldDoNothingWhenConfigMapIsDeleted() {
        var request = new Reconciler.Request(SystemSetting.SYSTEM_CONFIG);
        systemConfigMap.getMetadata().setDeletionTimestamp(Instant.now());
        when(client.fetch(ConfigMap.class, SystemSetting.SYSTEM_CONFIG))
            .thenReturn(Optional.of(systemConfigMap));

        reconciler.reconcile(request);

        verify(client, never()).update(any(ConfigMap.class));
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void reconcileShouldThrowExceptionForNonSystemConfig() {
        var request = new Reconciler.Request("other-config");

        assertThrows(IllegalStateException.class, () -> reconciler.reconcile(request));
    }

    @Test
    void reconcileShouldUpdateChecksumAndPublishEventWhenDataChanges() {
        var data = Map.of("key1", "value1", "key2", "value2");
        systemConfigMap.setData(data);

        when(client.fetch(ConfigMap.class, SystemSetting.SYSTEM_CONFIG))
            .thenReturn(Optional.of(systemConfigMap));
        when(client.fetch(ConfigMap.class, SystemSetting.SYSTEM_CONFIG_DEFAULT))
            .thenReturn(Optional.of(defaultConfigMap));

        var request = new Reconciler.Request(SystemSetting.SYSTEM_CONFIG);
        reconciler.reconcile(request);

        // Verify checksum annotation was added
        verify(client, times(1)).update(argThat(configMap -> {
            var annotations = configMap.getMetadata().getAnnotations();
            return annotations != null
                && annotations.containsKey(Constant.CHECKSUM_CONFIG_ANNO)
                && annotations.containsKey("halo.run/data-snapshot");
        }));

        // Verify event was published
        ArgumentCaptor<SystemConfigChangedEvent> eventCaptor =
            ArgumentCaptor.forClass(SystemConfigChangedEvent.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());

        var event = eventCaptor.getValue();
        assertThat(event.getNewData()).isEqualTo(data);
    }

    @Test
    void reconcileShouldNotUpdateWhenChecksumUnchanged() {
        var data = Map.of("key1", "value1");
        systemConfigMap.setData(data);

        // Pre-set checksum to match current data - sha256 of data.toString()
        var existingChecksum = com.google.common.hash.Hashing.sha256()
            .hashString(systemConfigMap.getData().toString(),
                java.nio.charset.StandardCharsets.UTF_8)
            .toString();
        systemConfigMap.getMetadata().getAnnotations()
            .put(Constant.CHECKSUM_CONFIG_ANNO, existingChecksum);

        when(client.fetch(ConfigMap.class, SystemSetting.SYSTEM_CONFIG))
            .thenReturn(Optional.of(systemConfigMap));

        var request = new Reconciler.Request(SystemSetting.SYSTEM_CONFIG);
        reconciler.reconcile(request);

        verify(client, never()).update(any(ConfigMap.class));
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void reconcileShouldHandleNullData() {
        systemConfigMap.setData(null);

        when(client.fetch(ConfigMap.class, SystemSetting.SYSTEM_CONFIG))
            .thenReturn(Optional.of(systemConfigMap));
        when(client.fetch(ConfigMap.class, SystemSetting.SYSTEM_CONFIG_DEFAULT))
            .thenReturn(Optional.of(defaultConfigMap));

        var request = new Reconciler.Request(SystemSetting.SYSTEM_CONFIG);
        reconciler.reconcile(request);

        // Should still update with checksum
        verify(client, times(1)).update(any(ConfigMap.class));
        verify(eventPublisher, times(1)).publishEvent(any(SystemConfigChangedEvent.class));
    }

    @Test
    void reconcileShouldMergeWithDefaultConfig() {
        var userData = Map.of("user.key", "user-value");
        var defaultData = Map.of("default.key", "default-value");

        systemConfigMap.setData(userData);
        defaultConfigMap.setData(defaultData);

        when(client.fetch(ConfigMap.class, SystemSetting.SYSTEM_CONFIG))
            .thenReturn(Optional.of(systemConfigMap));
        when(client.fetch(ConfigMap.class, SystemSetting.SYSTEM_CONFIG_DEFAULT))
            .thenReturn(Optional.of(defaultConfigMap));

        var request = new Reconciler.Request(SystemSetting.SYSTEM_CONFIG);
        reconciler.reconcile(request);

        ArgumentCaptor<SystemConfigChangedEvent> eventCaptor =
            ArgumentCaptor.forClass(SystemConfigChangedEvent.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());

        var event = eventCaptor.getValue();
        // New data should contain user data
        assertThat(event.getNewData()).containsEntry("user.key", "user-value");
    }

    @Test
    void reconcileShouldHandleEmptyData() {
        systemConfigMap.setData(Map.of());

        when(client.fetch(ConfigMap.class, SystemSetting.SYSTEM_CONFIG))
            .thenReturn(Optional.of(systemConfigMap));
        when(client.fetch(ConfigMap.class, SystemSetting.SYSTEM_CONFIG_DEFAULT))
            .thenReturn(Optional.of(defaultConfigMap));

        var request = new Reconciler.Request(SystemSetting.SYSTEM_CONFIG);
        reconciler.reconcile(request);

        verify(client, times(1)).update(any(ConfigMap.class));
        verify(eventPublisher, times(1)).publishEvent(any(SystemConfigChangedEvent.class));
    }

    @Test
    void reconcileShouldPreserveDataSnapshotForComparison() {
        var oldData = Map.of("key1", "old-value");
        var newData = Map.of("key1", "new-value");

        // Set initial data and snapshot
        systemConfigMap.setData(oldData);
        systemConfigMap.getMetadata().getAnnotations()
            .put("halo.run/data-snapshot", "{\"key1\":\"old-value\"}");

        // Update to new data
        systemConfigMap.setData(newData);

        when(client.fetch(ConfigMap.class, SystemSetting.SYSTEM_CONFIG))
            .thenReturn(Optional.of(systemConfigMap));
        when(client.fetch(ConfigMap.class, SystemSetting.SYSTEM_CONFIG_DEFAULT))
            .thenReturn(Optional.of(defaultConfigMap));

        var request = new Reconciler.Request(SystemSetting.SYSTEM_CONFIG);
        reconciler.reconcile(request);

        ArgumentCaptor<SystemConfigChangedEvent> eventCaptor =
            ArgumentCaptor.forClass(SystemConfigChangedEvent.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());

        var event = eventCaptor.getValue();
        assertThat(event.getOldData()).containsEntry("key1", "old-value");
        assertThat(event.getNewData()).containsEntry("key1", "new-value");
    }

    @Test
    void reconcileShouldHandleNoDefaultConfig() {
        var data = Map.of("key1", "value1");
        systemConfigMap.setData(data);

        when(client.fetch(ConfigMap.class, SystemSetting.SYSTEM_CONFIG))
            .thenReturn(Optional.of(systemConfigMap));
        when(client.fetch(ConfigMap.class, SystemSetting.SYSTEM_CONFIG_DEFAULT))
            .thenReturn(Optional.empty());

        var request = new Reconciler.Request(SystemSetting.SYSTEM_CONFIG);
        reconciler.reconcile(request);

        verify(client, times(1)).update(any(ConfigMap.class));

        ArgumentCaptor<SystemConfigChangedEvent> eventCaptor =
            ArgumentCaptor.forClass(SystemConfigChangedEvent.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());

        var event = eventCaptor.getValue();
        assertThat(event.getNewData()).isEqualTo(data);
    }

    private ConfigMap createConfigMap(String name) {
        var configMap = new ConfigMap();
        var metadata = new Metadata();
        metadata.setName(name);
        metadata.setAnnotations(new HashMap<>());
        configMap.setMetadata(metadata);
        configMap.setData(new HashMap<>());
        return configMap;
    }
}