package run.halo.app.core.extension.reconciler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.utils.JsonUtils;

/**
 * Tests for {@link SystemSettingReconciler}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class SystemSettingReconcilerTest {

    @Mock
    private ExtensionClient client;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private SystemConfigurableEnvironmentFetcher environmentFetcher;

    private SystemSettingReconciler systemSettingReconciler;

    @BeforeEach
    void setUp() {
        systemSettingReconciler = new SystemSettingReconciler(client, environmentFetcher,
            applicationContext);
    }

    @Test
    void reconcileArchivesRouteRule() {
        ConfigMap configMap = systemConfigMapForRouteRule(rules -> {
            rules.setArchives("archives-new");
            return rules;
        });
        when(environmentFetcher.getConfigMapBlocking()).thenReturn(Optional.of(configMap));
        when(client.fetch(eq(ConfigMap.class), eq(SystemSetting.SYSTEM_CONFIG)))
            .thenReturn(Optional.of(configMap));
        systemSettingReconciler.reconcile(new Reconciler.Request(SystemSetting.SYSTEM_CONFIG));
        ArgumentCaptor<ConfigMap> captor = ArgumentCaptor.forClass(ConfigMap.class);
        verify(client, times(1)).update(captor.capture());

        ConfigMap updatedConfigMap = captor.getValue();
        assertThat(rulesFrom(updatedConfigMap).getArchives()).isEqualTo("archives-new");
        assertThat(rulesFrom(updatedConfigMap).getPost()).isEqualTo("/archives-new/{slug}");

        assertThat(oldRulesFromAnno(updatedConfigMap).getArchives()).isEqualTo("archives-new");
        assertThat(oldRulesFromAnno(updatedConfigMap).getPost()).isEqualTo("/archives-new/{slug}");

        // archives and post
        verify(applicationContext, times(2)).publishEvent(any());
    }

    @Test
    void reconcileTagsRule() {
        ConfigMap configMap = systemConfigMapForRouteRule(rules -> {
            rules.setTags("tags-new");
            return rules;
        });
        when(environmentFetcher.getConfigMapBlocking()).thenReturn(Optional.of(configMap));
        when(client.fetch(eq(ConfigMap.class), eq(SystemSetting.SYSTEM_CONFIG)))
            .thenReturn(Optional.of(configMap));
        systemSettingReconciler.reconcile(new Reconciler.Request(SystemSetting.SYSTEM_CONFIG));
        ArgumentCaptor<ConfigMap> captor = ArgumentCaptor.forClass(ConfigMap.class);
        verify(client, times(1)).update(captor.capture());

        ConfigMap updatedConfigMap = captor.getValue();
        assertThat(rulesFrom(updatedConfigMap).getTags()).isEqualTo("tags-new");

        assertThat(oldRulesFromAnno(updatedConfigMap).getTags()).isEqualTo("tags-new");

        verify(applicationContext, times(1)).publishEvent(any());
    }

    @Test
    void reconcileCategoriesRule() {
        ConfigMap configMap = systemConfigMapForRouteRule(rules -> {
            rules.setCategories("categories-new");
            return rules;
        });
        when(environmentFetcher.getConfigMapBlocking()).thenReturn(Optional.of(configMap));
        when(client.fetch(eq(ConfigMap.class), eq(SystemSetting.SYSTEM_CONFIG)))
            .thenReturn(Optional.of(configMap));
        systemSettingReconciler.reconcile(new Reconciler.Request(SystemSetting.SYSTEM_CONFIG));
        ArgumentCaptor<ConfigMap> captor = ArgumentCaptor.forClass(ConfigMap.class);
        verify(client, times(1)).update(captor.capture());

        ConfigMap updatedConfigMap = captor.getValue();
        assertThat(rulesFrom(updatedConfigMap).getCategories()).isEqualTo("categories-new");

        assertThat(oldRulesFromAnno(updatedConfigMap).getCategories()).isEqualTo("categories-new");

        verify(applicationContext, times(1)).publishEvent(any());
    }

    @Test
    void reconcilePostRule() {
        ConfigMap configMap = systemConfigMapForRouteRule(rules -> {
            rules.setPost("/post-new/{slug}");
            return rules;
        });
        when(environmentFetcher.getConfigMapBlocking()).thenReturn(Optional.of(configMap));
        when(client.fetch(eq(ConfigMap.class), eq(SystemSetting.SYSTEM_CONFIG)))
            .thenReturn(Optional.of(configMap));
        systemSettingReconciler.reconcile(new Reconciler.Request(SystemSetting.SYSTEM_CONFIG));
        ArgumentCaptor<ConfigMap> captor = ArgumentCaptor.forClass(ConfigMap.class);
        verify(client, times(1)).update(captor.capture());

        ConfigMap updatedConfigMap = captor.getValue();
        assertThat(rulesFrom(updatedConfigMap).getPost()).isEqualTo("/post-new/{slug}");

        assertThat(oldRulesFromAnno(updatedConfigMap).getPost()).isEqualTo("/post-new/{slug}");

        verify(applicationContext, times(1)).publishEvent(any());
    }

    private SystemSetting.ThemeRouteRules rulesFrom(ConfigMap configMap) {
        String s = configMap.getData().get(SystemSetting.ThemeRouteRules.GROUP);
        return JsonUtils.jsonToObject(s, SystemSetting.ThemeRouteRules.class);
    }

    private SystemSetting.ThemeRouteRules oldRulesFromAnno(ConfigMap configMap) {
        Map<String, String> annotations = configMap.getMetadata().getAnnotations();
        String s = annotations.get(SystemSettingReconciler.OLD_THEME_ROUTE_RULES);
        return JsonUtils.jsonToObject(s, SystemSetting.ThemeRouteRules.class);
    }

    private ConfigMap systemConfigMapForRouteRule(
        Function<SystemSetting.ThemeRouteRules, SystemSetting.ThemeRouteRules> function) {
        ConfigMap configMap = new ConfigMap();
        Metadata metadata = new Metadata();
        metadata.setName(SystemSetting.SYSTEM_CONFIG);
        configMap.setMetadata(metadata);

        SystemSetting.ThemeRouteRules themeRouteRules = new SystemSetting.ThemeRouteRules();
        themeRouteRules.setArchives("archives");
        themeRouteRules.setTags("tags");
        themeRouteRules.setCategories("categories");
        themeRouteRules.setPost("/archives/{slug}");
        Map<String, String> annotations = new HashMap<>();
        annotations.put(SystemSettingReconciler.OLD_THEME_ROUTE_RULES,
            JsonUtils.objectToJson(themeRouteRules));
        metadata.setAnnotations(annotations);

        SystemSetting.ThemeRouteRules newRules = function.apply(themeRouteRules);
        configMap.putDataItem(SystemSetting.ThemeRouteRules.GROUP,
            JsonUtils.objectToJson(newRules));
        return configMap;
    }

    @Test
    void changePostPatternPrefixIfNecessary() {
        SystemSetting.ThemeRouteRules newRouteRules = new SystemSetting.ThemeRouteRules();
        newRouteRules.setPost("/archives/{slug}");
        newRouteRules.setArchives("new");
        boolean result = SystemSettingReconciler.RouteRuleReconciler
            .changePostPatternPrefixIfNecessary("archives", newRouteRules);
        assertThat(result).isTrue();

        assertThat(newRouteRules.getPost()).isEqualTo("/new/{slug}");
    }
}