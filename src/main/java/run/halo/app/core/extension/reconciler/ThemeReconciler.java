package run.halo.app.core.extension.reconciler;

import java.io.IOException;
import java.nio.file.Path;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.FileSystemUtils;
import run.halo.app.core.extension.Setting;
import run.halo.app.core.extension.Theme;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.controller.Reconciler.Request;
import run.halo.app.infra.exception.ThemeUninstallException;
import run.halo.app.infra.properties.HaloProperties;
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
            });
        return new Result(false, null);
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
