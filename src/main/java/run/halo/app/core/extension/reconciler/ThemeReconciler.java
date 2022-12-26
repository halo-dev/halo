package run.halo.app.core.extension.reconciler;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.FileSystemUtils;
import run.halo.app.core.extension.AnnotationSetting;
import run.halo.app.core.extension.Setting;
import run.halo.app.core.extension.Theme;
import run.halo.app.core.extension.theme.SettingUtils;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ExtensionUtil;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
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
@Component
public class ThemeReconciler implements Reconciler<Request> {
    private static final String FINALIZER_NAME = "theme-protection";

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
                    cleanUpResourcesAndRemoveFinalizer(request.name());
                    return;
                }
                addFinalizerIfNecessary(theme);
                themeSettingDefaultConfig(theme);
                reconcileStatus(request.name());
            });
        return new Result(false, null);
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return builder
            .extension(new Theme())
            .build();
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
                var data = SettingUtils.settingDefinedDefaultValueMap(setting);
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

    private void addFinalizerIfNecessary(Theme oldTheme) {
        Set<String> finalizers = oldTheme.getMetadata().getFinalizers();
        if (finalizers != null && finalizers.contains(FINALIZER_NAME)) {
            return;
        }
        client.fetch(Theme.class, oldTheme.getMetadata().getName())
            .ifPresent(theme -> {
                Set<String> newFinalizers = theme.getMetadata().getFinalizers();
                if (newFinalizers == null) {
                    newFinalizers = new HashSet<>();
                    theme.getMetadata().setFinalizers(newFinalizers);
                }
                newFinalizers.add(FINALIZER_NAME);
                client.update(theme);
            });
    }

    private void cleanUpResourcesAndRemoveFinalizer(String themeName) {
        client.fetch(Theme.class, themeName).ifPresent(theme -> {
            reconcileThemeDeletion(theme);
            if (theme.getMetadata().getFinalizers() != null) {
                theme.getMetadata().getFinalizers().remove(FINALIZER_NAME);
            }
            client.update(theme);
        });
    }

    private void reconcileThemeDeletion(Theme theme) {
        deleteThemeFiles(theme);
        // delete theme setting form
        String settingName = theme.getSpec().getSettingName();
        if (StringUtils.isNotBlank(settingName)) {
            client.fetch(Setting.class, settingName)
                .ifPresent(client::delete);
        }
        // delete annotation setting
        deleteAnnotationSettings(theme.getMetadata().getName());
    }

    private void deleteAnnotationSettings(String themeName) {
        List<AnnotationSetting> result = client.list(AnnotationSetting.class, annotationSetting -> {
            Map<String, String> labels = ExtensionUtil.nullSafeLabels(annotationSetting);
            return themeName.equals(labels.get(Theme.THEME_NAME_LABEL));
        }, null);

        for (AnnotationSetting annotationSetting : result) {
            client.delete(annotationSetting);
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
