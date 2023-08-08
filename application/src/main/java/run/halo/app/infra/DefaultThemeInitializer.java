package run.halo.app.infra;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StreamUtils;
import run.halo.app.core.extension.theme.ThemeService;
import run.halo.app.infra.properties.HaloProperties;
import run.halo.app.infra.properties.ThemeProperties;
import run.halo.app.infra.utils.FileUtils;

@Slf4j
@Component
public class DefaultThemeInitializer implements ApplicationListener<SchemeInitializedEvent> {

    private final ThemeService themeService;

    private final ThemeRootGetter themeRoot;

    private final ThemeProperties themeProps;

    public DefaultThemeInitializer(ThemeService themeService, ThemeRootGetter themeRoot,
        HaloProperties haloProps) {
        this.themeService = themeService;
        this.themeRoot = themeRoot;
        this.themeProps = haloProps.getTheme();
    }

    @Override
    public void onApplicationEvent(SchemeInitializedEvent event) {
        if (themeProps.getInitializer().isDisabled()) {
            log.debug("Skipped initializing default theme due to disabled");
            return;
        }
        var themeRoot = this.themeRoot.get();
        var location = themeProps.getInitializer().getLocation();
        try {
            // TODO Checking if any themes are installed here in the future might be better?
            if (!FileUtils.isEmpty(themeRoot)) {
                log.debug("Skipped initializing default theme because there are themes "
                          + "inside theme root");
                return;
            }
            log.info("Initializing default theme from {}", location);
            var themeUrl = ResourceUtils.getURL(location);
            var content = DataBufferUtils.read(new UrlResource(themeUrl),
                DefaultDataBufferFactory.sharedInstance,
                StreamUtils.BUFFER_SIZE);
            var theme = themeService.install(content).block();
            log.info("Initialized default theme: {}", theme);
            // Because default active theme is default, we don't need to enabled it manually.
        } catch (IOException e) {
            // we should skip the initialization error at here
            log.warn("Failed to initialize theme from " + location, e);
        }
    }


}
