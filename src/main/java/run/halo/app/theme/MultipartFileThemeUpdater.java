package run.halo.app.theme;

import static run.halo.app.utils.FileUtils.copyFolder;
import static run.halo.app.utils.FileUtils.deleteFolderQuietly;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
import run.halo.app.exception.NotFoundException;
import run.halo.app.handler.theme.config.support.ThemeProperty;
import run.halo.app.repository.ThemeRepository;
import run.halo.app.utils.FileUtils;

/**
 * Multipart file theme updater.
 *
 * @author johnniang
 */
@Slf4j
public class MultipartFileThemeUpdater implements ThemeUpdater {

    private final MultipartFile file;

    private final ThemeFetcherComposite fetcherComposite;

    private final ThemeRepository themeRepository;

    public MultipartFileThemeUpdater(MultipartFile file,
            ThemeFetcherComposite fetcherComposite,
            ThemeRepository themeRepository) {
        this.file = file;
        this.fetcherComposite = fetcherComposite;
        this.themeRepository = themeRepository;
    }

    @Override
    public ThemeProperty update(String themeId) throws IOException {
        // check old theme id
        final var oldThemeProperty = this.themeRepository.fetchThemePropertyByThemeId(themeId)
                .orElseThrow(() -> new NotFoundException("主题 ID 为 " + themeId + " 不存在或已删除！"));

        // fetch new theme
        final var newThemeProperty = this.fetcherComposite.fetch(this.file);

        // delete old theme
        Path backupPath = this.backup(oldThemeProperty);

        themeRepository.deleteTheme(oldThemeProperty);

        // add new theme
        return themeRepository.attemptToAdd(newThemeProperty);
    }

    private Path backup(ThemeProperty themeProperty) throws IOException {
        final var themePath = Paths.get(themeProperty.getThemePath());
        Path tempDirectory = null;
        try {
            tempDirectory = FileUtils.createTempDirectory();
            copyFolder(themePath, tempDirectory);
            log.info("Backup theme: {} to {} successfully!", themeProperty.getId(), tempDirectory);
            return tempDirectory;
        } catch (IOException e) {
            // clear temp directory
            deleteFolderQuietly(tempDirectory);
            throw e;
        }
    }
}
