package run.halo.app.core.extension.reconciler;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.FileSystemUtils;
import run.halo.app.core.extension.Setting;
import run.halo.app.core.extension.Theme;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.controller.Reconciler.Request;
import run.halo.app.infra.exception.ThemeUninstallException;
import run.halo.app.infra.properties.HaloProperties;
import run.halo.app.infra.utils.JsonUtils;
import run.halo.app.theme.ThemePathPolicy;

/**
 * Reconciler for theme.
 *
 * @author guqing
 * @since 2.0.0
 */
public class ThemeReconciler implements Reconciler<Request> {

    private final ExtensionClient client;
    private final ThemePathPolicy themePathPolicy;

    public ThemeReconciler(ExtensionClient client, HaloProperties haloProperties) {
        this.client = client;
        themePathPolicy = new ThemePathPolicy(haloProperties.getWorkDir());
    }

    @Override
    public Result reconcile(Request request) {
        client.fetch(Theme.class, request.name())
            .ifPresent(theme -> {
                if (isDeleted(theme)) {
                    reconcileThemeDeletion(theme);
                }
                themeSettingDefaultConfig(theme);
                reconcileStatus(request.name());
            });
        return new Result(false, null);
    }

    private void reconcileStatus(String name) {
        client.fetch(Theme.class, name).ifPresent(theme -> {
            Theme oldTheme = JsonUtils.deepCopy(theme);
            if (theme.getStatus() == null) {
                theme.setStatus(new Theme.ThemeStatus());
            }
            Path themePath = themePathPolicy.generate(theme);
            theme.getStatus().setLocation(themePath.toAbsolutePath().toString());
            if (!oldTheme.equals(theme)) {
                client.update(theme);
            }
        });
    }

    private void themeSettingDefaultConfig(Theme theme) {
        if (StringUtils.isBlank(theme.getSpec().getSettingName())) {
            return;
        }
        final String userDefinedConfigMapName = theme.getSpec().getConfigMapName();

        final String newConfigMapName = UUID.randomUUID().toString();
        if (StringUtils.isBlank(userDefinedConfigMapName)) {
            client.fetch(Theme.class, theme.getMetadata().getName())
                .ifPresent(themeToUse -> {
                    Theme oldTheme = JsonUtils.deepCopy(themeToUse);
                    themeToUse.getSpec().setConfigMapName(newConfigMapName);
                    if (!oldTheme.equals(themeToUse)) {
                        client.update(themeToUse);
                    }
                });
        }

        final String configMapNameToUse =
            StringUtils.defaultIfBlank(userDefinedConfigMapName, newConfigMapName);

        boolean existConfigMap = client.fetch(ConfigMap.class, configMapNameToUse)
            .isPresent();
        if (existConfigMap) {
            return;
        }

        client.fetch(Setting.class, theme.getSpec().getSettingName())
            .ifPresent(setting -> {
                Map<String, String> data = settingDefinedDefaultValueMap(setting);
                if (CollectionUtils.isEmpty(data)) {
                    return;
                }
                ConfigMap configMap = new ConfigMap();
                configMap.setMetadata(new Metadata());
                configMap.getMetadata().setName(configMapNameToUse);
                configMap.setData(data);
                client.create(configMap);
            });
    }

    Map<String, String> settingDefinedDefaultValueMap(Setting setting) {
        final String defaultValueField = "value";
        final String nameField = "name";
        List<Setting.SettingForm> forms = setting.getSpec().getForms();
        if (CollectionUtils.isEmpty(forms)) {
            return null;
        }
        Map<String, String> data = new LinkedHashMap<>();
        for (Setting.SettingForm form : forms) {
            String group = form.getGroup();
            Map<String, JsonNode> groupValue = form.getFormSchema().stream()
                .map(o -> JsonUtils.DEFAULT_JSON_MAPPER.convertValue(o, JsonNode.class))
                .filter(jsonNode -> jsonNode.isObject() && jsonNode.has(nameField)
                    && jsonNode.has(defaultValueField))
                .map(jsonNode -> {
                    String name = jsonNode.findValue(nameField).asText();
                    JsonNode value = jsonNode.findValue(defaultValueField);
                    return Map.entry(name, value);
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            data.put(group, JsonUtils.objectToJson(groupValue));
        }
        return data;
    }

    private void reconcileThemeDeletion(Theme theme) {
        deleteThemeFiles(theme);
        // delete theme setting form
        String settingName = theme.getSpec().getSettingName();
        if (StringUtils.isNotBlank(settingName)) {
            client.fetch(Setting.class, settingName)
                .ifPresent(client::delete);
        }
    }

    private void deleteThemeFiles(Theme theme) {
        Path themeDir = themePathPolicy.generate(theme);
        try {
            FileSystemUtils.deleteRecursively(themeDir);
        } catch (IOException e) {
            throw new ThemeUninstallException("Failed to delete theme files.", e);
        }
    }

    private boolean isDeleted(Theme theme) {
        return theme.getMetadata().getDeletionTimestamp() != null;
    }
}
