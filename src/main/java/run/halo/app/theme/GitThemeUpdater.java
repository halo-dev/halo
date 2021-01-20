package run.halo.app.theme;

import java.io.IOException;
import run.halo.app.exception.NotFoundException;
import run.halo.app.handler.theme.config.support.ThemeProperty;
import run.halo.app.repository.ThemeRepository;

/**
 * Update from theme property config.
 *
 * @author johnniang
 */
public class GitThemeUpdater implements ThemeUpdater {

    private final ThemeRepository themeRepository;

    private final ThemeFetcherComposite fetcherComposite;

    private final ConflictStrategy conflictStrategy;

    public GitThemeUpdater(ThemeRepository themeRepository,
            ThemeFetcherComposite fetcherComposite,
            ConflictStrategy conflictStrategy) {
        this.themeRepository = themeRepository;
        this.fetcherComposite = fetcherComposite;
        this.conflictStrategy = conflictStrategy;
    }

    @Override
    public ThemeProperty update(String themeId) throws IOException {
        // get theme property
        final var oldThemeProperty = themeRepository.fetchThemePropertyByThemeId(themeId)
                .orElseThrow(() -> new NotFoundException("主题 " + themeId + " 不存在或以删除！").setErrorData(themeId));

        // get update config
        final var gitRepo = oldThemeProperty.getRepo();

        // fetch latest theme
        final var newThemeProperty = fetcherComposite.fetch(gitRepo);

        // merge old theme and new theme
        final var mergedThemeProperty = merge(oldThemeProperty, newThemeProperty);

        // backup old theme
        final var backupPath = ThemeUpdater.backup(oldThemeProperty);

        try {
            // delete old theme
            themeRepository.deleteTheme(oldThemeProperty);

            // copy new theme to old theme folder
            return themeRepository.attemptToAdd(mergedThemeProperty);
        } catch (Throwable t) {
            log.error("Failed to add new theme, and restoring old theme from " + backupPath, t);
            // restore old theme
            ThemeUpdater.restore(backupPath, oldThemeProperty);
            log.info("Restored old theme from path: {}", backupPath);
            throw t;
        }
    }

    public ThemeProperty merge(ThemeProperty oldThemeProperty, ThemeProperty newThemeProperty) {
        //TODO Complete merging process
        return newThemeProperty;
    }

    /**
     * Update strategy when there are conflicts.
     *
     * @author johnniang
     */
    public enum ConflictStrategy {

        /**
         * Override old theme.
         */
        OVERRIDE,

        /**
         * Cancel update theme.
         */
        CANCEL

    }
}
