package run.halo.app.theme;

import java.io.IOException;
import org.eclipse.jgit.api.errors.GitAPIException;
import run.halo.app.exception.ThemePropertyMissingException;
import run.halo.app.handler.theme.config.support.ThemeProperty;
import run.halo.app.utils.FileUtils;

import static run.halo.app.utils.GitUtils.cloneFromGit;

/**
 * Git theme fetcher.
 *
 * @author johnniang
 */
public class GitThemeFetcher implements ThemeFetcher {

    @Override
    public boolean support(Object source) {
        if (source instanceof String) {
            return ((String) source).endsWith(".git");
        }
        return false;
    }

    @Override
    public ThemeProperty fetch(Object source) {
        final var repoUrl = source.toString();

        try {
            // create temp folder
            var tempDirectory = FileUtils.createTempDirectory();
            // clone from git
            cloneFromGit(repoUrl, tempDirectory);
            // locate theme property location
            var themePropertyPath = ThemeMetaLocator.INSTANCE.locateProperty(tempDirectory)
                    .orElseThrow(() -> new ThemePropertyMissingException("主题配置文件缺失，请确认后重试！"));

            // fetch property
            return ThemePropertyScanner.INSTANCE.fetchThemeProperty(themePropertyPath.getParent()).orElseThrow();
        } catch (IOException | GitAPIException e) {
            throw new RuntimeException("主题拉取失败！（" + e.getMessage() + "）", e);
        }
    }

}
