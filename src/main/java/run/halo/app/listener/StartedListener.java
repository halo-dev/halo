package run.halo.app.listener;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.internal.jdbc.JdbcUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;
import run.halo.app.config.properties.HaloProperties;
import run.halo.app.model.properties.PrimaryProperties;
import run.halo.app.model.support.HaloConst;
import run.halo.app.service.OptionService;
import run.halo.app.service.ThemeService;
import run.halo.app.utils.FileUtils;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Collections;

/**
 * The method executed after the application is started.
 *
 * @author ryanwang
 * @author guqing
 * @date 2018-12-05
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

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        try {
            this.migrate();
        } catch (SQLException e) {
            log.error("Failed to migrate database!", e);
        }
        this.initThemes();
        this.initDirectory();
        this.printStartInfo();
    }

    private void printStartInfo() {
        String blogUrl = optionService.getBlogBaseUrl();
        log.info(AnsiOutput.toString(AnsiColor.BRIGHT_BLUE, "Halo started at         ", blogUrl));
        log.info(AnsiOutput.toString(AnsiColor.BRIGHT_BLUE, "Halo admin started at   ", blogUrl, "/", haloProperties.getAdminPath()));
        if (!haloProperties.isDocDisabled()) {
            log.debug(AnsiOutput.toString(AnsiColor.BRIGHT_BLUE, "Halo api doc was enabled at  ", blogUrl, "/swagger-ui.html"));
        }
        log.info(AnsiOutput.toString(AnsiColor.BRIGHT_YELLOW, "Halo has started successfully!"));
    }

    /**
     * Migrate database.
     */
    private void migrate() throws SQLException {
        log.info("Starting migrate database...");

        Flyway flyway = Flyway
                .configure()
                .locations("classpath:/migration")
                .baselineVersion("1")
                .baselineOnMigrate(true)
                .dataSource(url, username, password)
                .load();
        flyway.repair();
        flyway.migrate();

        // Gets database connection
        Connection connection = flyway.getConfiguration().getDataSource().getConnection();

        // Gets database metadata
        DatabaseMetaData databaseMetaData = JdbcUtils.getDatabaseMetaData(connection);

        // Gets database product name
        HaloConst.DATABASE_PRODUCT_NAME = databaseMetaData.getDatabaseProductName() + " " + databaseMetaData.getDatabaseProductVersion();

        // Close connection.
        connection.close();

        log.info("Migrate database succeed.");
    }

    /**
     * Init internal themes
     */
    private void initThemes() {
        // Whether the blog has initialized
        Boolean isInstalled = optionService.getByPropertyOrDefault(PrimaryProperties.IS_INSTALLED, Boolean.class, false);
        try {
            String themeClassPath = ResourceUtils.CLASSPATH_URL_PREFIX + ThemeService.THEME_FOLDER;

            URI themeUri = ResourceUtils.getURL(themeClassPath).toURI();

            log.debug("Theme uri: [{}]", themeUri);

            Path source;

            if ("jar".equalsIgnoreCase(themeUri.getScheme())) {

                // Create new file system for jar
                FileSystem fileSystem = getFileSystem(themeUri);
                source = fileSystem.getPath("/BOOT-INF/classes/" + ThemeService.THEME_FOLDER);
            } else {
                source = Paths.get(themeUri);
            }

            // Create theme folder
            Path themePath = themeService.getBasePath();

            // Fix the problem that the project cannot start after moving to a new server
            if (!haloProperties.isProductionEnv() || Files.notExists(themePath) || !isInstalled) {
                FileUtils.copyFolder(source, themePath);
                log.debug("Copied theme folder from [{}] to [{}]", source, themePath);
            } else {
                log.debug("Skipped copying theme folder due to existence of theme folder");
            }
        } catch (Exception e) {
            log.error("Initialize internal theme to user path error!", e);
        }
    }

    @NonNull
    private FileSystem getFileSystem(@NonNull URI uri) throws IOException {
        Assert.notNull(uri, "Uri must not be null");

        FileSystem fileSystem;

        try {
            fileSystem = FileSystems.getFileSystem(uri);
        } catch (FileSystemNotFoundException e) {
            fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
        }

        return fileSystem;
    }

    private void initDirectory() {
        Path workPath = Paths.get(haloProperties.getWorkDir());
        Path backupPath = Paths.get(haloProperties.getBackupDir());
        Path dataExportPath = Paths.get(haloProperties.getDataExportDir());

        try {
            if (Files.notExists(workPath)) {
                Files.createDirectories(workPath);
                log.info("Created work directory: [{}]", workPath);
            }

            if (Files.notExists(backupPath)) {
                Files.createDirectories(backupPath);
                log.info("Created backup directory: [{}]", backupPath);
            }

            if (Files.notExists(dataExportPath)) {
                Files.createDirectories(dataExportPath);
                log.info("Created data export directory: [{}]", dataExportPath);
            }
        } catch (IOException ie) {
            throw new RuntimeException("Failed to initialize directories", ie);
        }
    }
}
