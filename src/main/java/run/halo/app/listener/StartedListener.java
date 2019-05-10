package run.halo.app.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.util.ResourceUtils;
import run.halo.app.config.properties.HaloProperties;
import run.halo.app.model.entity.User;
import run.halo.app.model.params.UserParam;
import run.halo.app.model.properties.PrimaryProperties;
import run.halo.app.model.support.CreateCheck;
import run.halo.app.service.OptionService;
import run.halo.app.service.ThemeService;
import run.halo.app.service.UserService;
import run.halo.app.utils.FileUtils;
import run.halo.app.utils.ValidationUtils;

import java.net.URI;
import java.nio.file.*;
import java.util.Collections;
import java.util.List;

/**
 * The method executed after the application is started.
 *
 * @author ryanwang
 * @date : 2018/12/5
 */
@Slf4j
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class StartedListener implements ApplicationListener<ApplicationStartedEvent> {

    @Autowired
    private HaloProperties haloProperties;

    @Autowired
    private OptionService optionService;

    @Autowired
    private ThemeService themeService;

    @Autowired
    private UserService userService;

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        // save halo version to database
        this.printStartInfo();
        this.initThemes();
    }

    private void printStartInfo() {
        String blogUrl = optionService.getBlogBaseUrl();

        log.info("Halo started at         {}", blogUrl);
        log.info("Halo admin started at   {}/admin", blogUrl);
        if (!haloProperties.isDocDisabled()) {
            log.debug("Halo doc was enable at  {}/swagger-ui.html", blogUrl);
        }
    }

    /**
     * Init internal themes
     */
    private void initThemes() {
        // Whether the blog has initialized
        Boolean isInstalled = optionService.getByPropertyOrDefault(PrimaryProperties.IS_INSTALLED, Boolean.class, false);

        if (haloProperties.isProductionEnv() && isInstalled) {
            // Skip
            return;
        }

        try {
            String themeClassPath = ResourceUtils.CLASSPATH_URL_PREFIX + ThemeService.THEME_FOLDER;

            URI themeUri = ResourceUtils.getURL(themeClassPath).toURI();

            Path source;

            if (themeUri.getScheme().equalsIgnoreCase("jar")) {
                // Create new file system for jar
                FileSystem fileSystem = FileSystems.newFileSystem(themeUri, Collections.emptyMap());
                source = fileSystem.getPath("/BOOT-INF/classes/" + ThemeService.THEME_FOLDER);
            } else {
                source = Paths.get(themeUri);
            }

            // Create theme folder
            Path themePath = themeService.getBasePath();

            if (!haloProperties.isProductionEnv() || Files.notExists(themePath)) {
                FileUtils.copyFolder(source, themePath);
                log.info("Copied theme folder from [{}] to [{}]", source, themePath);
            } else {
                log.info("Skipped copying theme folder due to existence of theme folder");
            }
        } catch (Exception e) {
            throw new RuntimeException("Initialize internal theme to user path error", e);
        }
    }

}
