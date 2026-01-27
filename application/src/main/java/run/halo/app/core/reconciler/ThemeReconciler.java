package run.halo.app.core.reconciler;

import static run.halo.app.extension.ExtensionUtil.addFinalizers;
import static run.halo.app.extension.ExtensionUtil.isDeleted;
import static run.halo.app.extension.ExtensionUtil.removeFinalizers;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.retry.RetryException;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.backoff.FixedBackOff;
import reactor.core.Exceptions;
import run.halo.app.core.extension.AnnotationSetting;
import run.halo.app.core.extension.Setting;
import run.halo.app.core.extension.Theme;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.controller.Reconciler.Request;
import run.halo.app.infra.Condition;
import run.halo.app.infra.ConditionStatus;
import run.halo.app.infra.SystemVersionSupplier;
import run.halo.app.infra.ThemeRootGetter;
import run.halo.app.infra.exception.ThemeUninstallException;
import run.halo.app.infra.utils.ReactiveUtils;
import run.halo.app.infra.utils.SettingUtils;
import run.halo.app.infra.utils.VersionUtils;
import run.halo.app.theme.TemplateEngineManager;

/**
 * Reconciler for theme.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
@RequiredArgsConstructor
public class ThemeReconciler implements Reconciler<Request> {
    private static final Duration BLOCKING_TIMEOUT = ReactiveUtils.DEFAULT_TIMEOUT;
    private static final String FINALIZER_NAME = "theme-protection";

    private final ExtensionClient client;

    private final ThemeRootGetter themeRoot;
    private final SystemVersionSupplier systemVersionSupplier;
    private final TemplateEngineManager templateEngineManager;

    private final RetryTemplate retryTemplate = new RetryTemplate(RetryPolicy.builder()
        .backOff(new FixedBackOff(300, 20))
        .predicate(IllegalStateException.class::isInstance)
        .build());

    @Override
    public Result reconcile(Request request) {
        client.fetch(Theme.class, request.name())
            .ifPresent(theme -> {
                if (isDeleted(theme)) {
                    if (removeFinalizers(theme.getMetadata(), Set.of(FINALIZER_NAME))) {
                        cleanUpResources(theme);
                        client.update(theme);
                    }
                    return;
                }
                addFinalizers(theme.getMetadata(), Set.of(FINALIZER_NAME));

                themeSettingDefaultConfig(theme);
                reconcileStatus(theme);
                client.update(theme);
            });
        return new Result(false, null);
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return builder
            .extension(new Theme())
            .build();
    }

    void reconcileStatus(Theme theme) {
        var status = theme.getStatus();
        if (status == null) {
            status = new Theme.ThemeStatus();
            theme.setStatus(status);
        }
        var name = theme.getMetadata().getName();
        var themePath = themeRoot.get().resolve(name);
        status.setLocation(themePath.toAbsolutePath().toString());

        status.setPhase(Theme.ThemePhase.READY);
        var conditionBuilder = Condition.builder()
            .type(Theme.ThemePhase.READY.name())
            .status(ConditionStatus.TRUE)
            .reason(Theme.ThemePhase.READY.name())
            .message(StringUtils.EMPTY)
            .lastTransitionTime(Instant.now());

        // Check if this theme version is match requires param.
        var normalVersion = systemVersionSupplier.get().toStableVersion().toString();
        var requires = theme.getSpec().getRequires();
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
    }

    private void themeSettingDefaultConfig(Theme theme) {
        var spec = theme.getSpec();
        var settingName = spec.getSettingName();
        if (StringUtils.isBlank(settingName)) {
            return;
        }
        var configMapName = spec.getConfigMapName();
        if (StringUtils.isBlank(configMapName)) {
            configMapName = UUID.randomUUID().toString();
        }
        spec.setConfigMapName(configMapName);
        SettingUtils.createOrUpdateConfigMap(client, settingName, configMapName);
    }

    private void cleanUpResources(Theme theme) {
        reconcileThemeDeletion(theme);
    }

    private void reconcileThemeDeletion(Theme theme) {
        templateEngineManager.clearCache(theme.getMetadata().getName()).block(BLOCKING_TIMEOUT);
        // delete theme setting form
        var settingName = theme.getSpec().getSettingName();
        if (StringUtils.isNotBlank(settingName)) {
            client.fetch(Setting.class, settingName).ifPresent(client::delete);
            try {
                retryTemplate.execute(() -> {
                    client.fetch(Setting.class, settingName).ifPresent(setting -> {
                        throw new IllegalStateException("Waiting for setting to be deleted.");
                    });
                    return null;
                });
            } catch (RetryException e) {
                throw Exceptions.propagate(e);
            }
        }
        // delete annotation setting
        deleteAnnotationSettings(theme.getMetadata().getName());
        deleteThemeFiles(theme);
    }

    private void deleteAnnotationSettings(String themeName) {
        var result = listAnnotationSettingsByThemeName(themeName);

        for (AnnotationSetting annotationSetting : result) {
            client.delete(annotationSetting);
        }

        try {
            retryTemplate.execute(() -> {
                var annotationSettings = listAnnotationSettingsByThemeName(themeName);
                if (annotationSettings.isEmpty()) {
                    return null;
                }
                throw new IllegalStateException("Waiting for annotation settings to be deleted.");
            });
        } catch (RetryException e) {
            throw Exceptions.propagate(e);
        }
    }

    private List<AnnotationSetting> listAnnotationSettingsByThemeName(String themeName) {
        return client.list(AnnotationSetting.class, annotationSetting -> {
            Map<String, String> labels = MetadataUtil.nullSafeLabels(annotationSetting);
            return themeName.equals(labels.get(Theme.THEME_NAME_LABEL));
        }, null);
    }

    private void deleteThemeFiles(Theme theme) {
        var themeDir = themeRoot.get().resolve(theme.getMetadata().getName());
        try {
            FileSystemUtils.deleteRecursively(themeDir);
        } catch (IOException e) {
            throw new ThemeUninstallException("Failed to delete theme files.", e);
        }
    }

}
