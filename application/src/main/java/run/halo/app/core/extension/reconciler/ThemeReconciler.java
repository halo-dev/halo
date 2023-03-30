package run.halo.app.core.extension.reconciler;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;
import run.halo.app.core.extension.AnnotationSetting;
import run.halo.app.core.extension.Setting;
import run.halo.app.core.extension.Theme;
import run.halo.app.core.extension.theme.SettingUtils;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.controller.Reconciler.Request;
import run.halo.app.infra.Condition;
import run.halo.app.infra.ConditionStatus;
import run.halo.app.infra.SystemVersionSupplier;
import run.halo.app.infra.exception.ThemeUninstallException;
import run.halo.app.infra.properties.HaloProperties;
import run.halo.app.infra.utils.JsonUtils;
import run.halo.app.infra.utils.VersionUtils;
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
    private final SystemVersionSupplier systemVersionSupplier;

    private final RetryTemplate retryTemplate = RetryTemplate.builder()
        .maxAttempts(20)
        .fixedBackoff(300)
        .retryOn(IllegalStateException.class)
        .build();

    public ThemeReconciler(ExtensionClient client, HaloProperties haloProperties,
        SystemVersionSupplier systemVersionSupplier) {
        this.client = client;
        themePathPolicy = new ThemePathPolicy(haloProperties.getWorkDir());
        this.systemVersionSupplier = systemVersionSupplier;
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

    void reconcileStatus(String name) {
        client.fetch(Theme.class, name).ifPresent(theme -> {
            final Theme.ThemeStatus status =
                defaultIfNull(theme.getStatus(), new Theme.ThemeStatus());
            final Theme.ThemeStatus oldStatus = JsonUtils.deepCopy(status);
            theme.setStatus(status);

            Path themePath = themePathPolicy.generate(theme);
            status.setLocation(themePath.toAbsolutePath().toString());

            status.setPhase(Theme.ThemePhase.READY);
            Condition.ConditionBuilder conditionBuilder = Condition.builder()
                .type(Theme.ThemePhase.READY.name())
                .status(ConditionStatus.TRUE)
                .reason(Theme.ThemePhase.READY.name())
                .message(StringUtils.EMPTY)
                .lastTransitionTime(Instant.now());

            // Check if this theme version is match requires param.
            String normalVersion = systemVersionSupplier.get().getNormalVersion();
            String requires = theme.getSpec().getRequires();
            if (!VersionUtils.satisfiesRequires(normalVersion, requires)) {
                status.setPhase(Theme.ThemePhase.FAILED);
                conditionBuilder
                    .type(Theme.ThemePhase.FAILED.name())
                    .status(ConditionStatus.FALSE)
                    .reason("UnsatisfiedRequiresVersion")
                    .message(String.format(
                        "Theme requires a minimum system version of [%s], and you have [%s].",
                        requires, normalVersion));
            }
            Theme.nullSafeConditionList(theme).addAndEvictFIFO(conditionBuilder.build());

            if (!Objects.equals(oldStatus, status)) {
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
        SettingUtils.createOrUpdateConfigMap(client, theme.getSpec().getSettingName(),
            configMapNameToUse);
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
            retryTemplate.execute(callback -> {
                client.fetch(Setting.class, settingName).ifPresent(setting -> {
                    throw new IllegalStateException("Waiting for setting to be deleted.");
                });
                return null;
            });
        }
        // delete annotation setting
        deleteAnnotationSettings(theme.getMetadata().getName());
    }

    private void deleteAnnotationSettings(String themeName) {
        List<AnnotationSetting> result = listAnnotationSettingsByThemeName(themeName);

        for (AnnotationSetting annotationSetting : result) {
            client.delete(annotationSetting);
        }

        retryTemplate.execute(callback -> {
            List<AnnotationSetting> annotationSettings =
                listAnnotationSettingsByThemeName(themeName);
            if (annotationSettings.isEmpty()) {
                return null;
            }
            throw new IllegalStateException("Waiting for annotation settings to be deleted.");
        });
    }

    private List<AnnotationSetting> listAnnotationSettingsByThemeName(String themeName) {
        return client.list(AnnotationSetting.class, annotationSetting -> {
            Map<String, String> labels = MetadataUtil.nullSafeLabels(annotationSetting);
            return themeName.equals(labels.get(Theme.THEME_NAME_LABEL));
        }, null);
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
