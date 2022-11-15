package run.halo.app.infra;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import reactor.core.Exceptions;
import run.halo.app.core.extension.theme.ThemeService;
import run.halo.app.infra.utils.FileUtils;

@Slf4j
@Component
public class DefaultThemeInitializer implements ApplicationListener<SchemeInitializedEvent> {

    public static final String DEFAULT_THEME_URL = "classpath:themes/theme-earth.zip";

    private final ThemeService themeService;

    private final ThemeRootGetter themeRoot;

    public DefaultThemeInitializer(ThemeService themeService, ThemeRootGetter themeRoot) {
        this.themeService = themeService;
        this.themeRoot = themeRoot;
    }

    @Override
    public void onApplicationEvent(SchemeInitializedEvent event) {
        var themeRoot = this.themeRoot.get();
        try {
            // TODO Checking if any themes are installed here in the future might be better?
            if (!FileUtils.isEmpty(themeRoot)) {
                log.debug(
                    "Skipped initializing default theme because there are themes inside theme "
                        + "root");
                return;
            }
            log.info("Initializing default theme from {}", DEFAULT_THEME_URL);
            var defaultThemeUri = ResourceUtils.getURL(DEFAULT_THEME_URL).toURI();
            var latch = new CountDownLatch(1);
            themeService.install(Files.newInputStream(Path.of(defaultThemeUri)))
                .doFinally(signalType -> latch.countDown())
                .subscribe(theme -> log.info("Initialized default theme: {}",
                    theme.getMetadata().getName()));
            latch.await();
            // Because default active theme is default, we don't need to enabled it manually.
        } catch (IOException | URISyntaxException | InterruptedException e) {
            throw Exceptions.propagate(e);
        }
    }


}
