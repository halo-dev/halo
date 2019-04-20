package run.halo.app.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;
import run.halo.app.config.properties.HaloProperties;
import run.halo.app.model.entity.User;
import run.halo.app.model.params.UserParam;
import run.halo.app.model.properties.PrimaryProperties;
import run.halo.app.model.support.HaloConst;
import run.halo.app.service.OptionService;
import run.halo.app.service.ThemeService;
import run.halo.app.service.UserService;
import run.halo.app.utils.FileUtils;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The method executed after the application is started.
 *
 * @author : RYAN0UP
 * @date : 2018/12/5
 */
@Slf4j
@Configuration
public class StartedListener implements ApplicationListener<ApplicationStartedEvent> {

    @Autowired
    private HaloProperties haloProperties;

    @Autowired
    private OptionService optionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ThemeService themeService;

    @Autowired
    private UserService userService;

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        // save halo version to database
        this.cacheOwo();
        this.printStartInfo();
        this.initThemes();

        // Init user in development environment
        if (!haloProperties.isProductionEnv()) {
            initAnTestUserIfAbsent();
        }
    }

    /**
     * Initialize an test user if absent
     */
    private void initAnTestUserIfAbsent() {
        // Create an user if absent
        List<User> users = userService.listAll();

        if (users.isEmpty()) {
            UserParam userParam = new UserParam();
            userParam.setUsername("test");
            userParam.setNickname("developer");
            userParam.setEmail("test@test.com");

            log.debug("Initializing a test user: [{}]", userParam);

            User testUser = userService.createBy(userParam, "opentest");

            log.debug("Initialized a test user: [{}]", testUser);
        }
    }

    private void printStartInfo() {
        String blogUrl = optionService.getBlogBaseUrl();

        log.info("Halo started at         {}", blogUrl);
        // TODO admin may be changeable
        log.info("Halo admin started at   {}/admin", blogUrl);
        if (!haloProperties.isDocDisabled()) {
            log.debug("Halo doc was enable at  {}/swagger-ui.html", blogUrl);
        }
    }

    /**
     * Cache Owo
     */
    private void cacheOwo() {
        try {
            // The Map is LinkedHashMap
            @SuppressWarnings("unchecked")
            Map<String, String> owoMap = objectMapper.readValue(ResourceUtils.getURL("classpath:static/halo-common/OwO/OwO.path.json"), Map.class);

            HaloConst.OWO_MAP = Collections.unmodifiableMap(owoMap);
        } catch (IOException e) {
            log.error("Failed to read owo json", e);
            // TODO Consider to throw an exception
        }
    }

    /**
     * Init internal themes
     */
    private void initThemes() {
        // Whether the blog has initialized
        Boolean isInstalled = optionService.getByPropertyOrDefault(PrimaryProperties.IS_INSTALLED, Boolean.class, false);

        if (isInstalled) {
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
                log.info("Copying theme folder from [{}] to [{}]", source, themePath);

                FileUtils.copyFolder(source, themePath);
            } else {
                log.info("Skip copying theme folder due to existence of theme folder");
            }
        } catch (Exception e) {
            throw new RuntimeException("Init internal theme to user path error", e);
        }
    }

}
