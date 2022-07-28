package run.halo.app.theme;

import org.springframework.stereotype.Component;
import run.halo.app.core.extension.Theme;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.SystemSetting;

@Component
public class ThemeService {
    private final ExtensionClient client;
    private final SystemConfigurableEnvironmentFetcher systemConfigurableEnvironmentFetcher;

    public ThemeService(ExtensionClient client,
        SystemConfigurableEnvironmentFetcher systemConfigurableEnvironmentFetcher) {
        this.client = client;
        this.systemConfigurableEnvironmentFetcher = systemConfigurableEnvironmentFetcher;
    }

    public Theme getActive() {
        SystemSetting.Theme themeSetting =
            systemConfigurableEnvironmentFetcher.get(SystemSetting.THEME);
        if (themeSetting == null) {
            throw new IllegalStateException("Theme setting is not configured");
        }
        String activeTheme = themeSetting.getActive();
        return client.fetch(Theme.class, activeTheme).orElseThrow();
    }

    public Theme getTheme(String name) {
        return client.fetch(Theme.class, name).orElseThrow();
    }
}
