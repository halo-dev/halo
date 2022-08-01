package run.halo.app.theme.newplan;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.SystemSetting.Theme;
import run.halo.app.infra.properties.HaloProperties;

@Component
public class ThemeResolver {

    private final SystemConfigurableEnvironmentFetcher environmentFetcher;

    private final HaloProperties haloProperties;

    public ThemeResolver(SystemConfigurableEnvironmentFetcher environmentFetcher,
        HaloProperties haloProperties) {
        this.environmentFetcher = environmentFetcher;
        this.haloProperties = haloProperties;
    }

    public ThemeContext getTheme(ServerHttpRequest request) {
        var builder = ThemeContext.builder();
        var themeName = request.getQueryParams().getFirst("preview-theme");
        if (themeName == null) {
            // TODO Fetch activated theme name from other place.
            themeName = environmentFetcher.fetch(SystemSetting.THEME.getGroup(), Theme.class)
                .map(Theme::getActive)
                .orElse("default");
            builder.active(true);
        }
        // TODO Validate the existence of the theme name.

        var path = haloProperties.getWorkDir() + "/themes/" + themeName;

        return builder
            .name(themeName)
            .path(path)
            .build();
    }
}
