package run.halo.app.theme;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.TagOpt;
import run.halo.app.exception.ThemePropertyMissingException;
import run.halo.app.handler.theme.config.support.ThemeProperty;
import run.halo.app.utils.FileUtils;
import run.halo.app.utils.GitUtils;

/**
 * Git theme fetcher.
 *
 * @author johnniang
 */
@Slf4j
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
            final var tempDirectory = FileUtils.createTempDirectory();

            // clone from git
            log.info("Cloning git repo {} to {}", repoUrl, tempDirectory);
            try (final var git = Git.cloneRepository()
                .setTagOption(TagOpt.FETCH_TAGS)
                .setNoCheckout(false)
                .setDirectory(tempDirectory.toFile())
                .setCloneSubmodules(false)
                .setURI(repoUrl)
                .setRemote("upstream")
                .call()) {
                log.info("Cloned git repo {} to {} successfully", repoUrl, tempDirectory);

                // find latest tag
                final var latestTag = GitUtils.getLatestTag(git);
                final var checkoutCommand = git.checkout()
                    .setName("halo")
                    .setCreateBranch(true);
                if (latestTag != null) {
                    // checkout latest tag
                    checkoutCommand.setStartPoint(latestTag.getValue());
                }
                Ref haloBranch = checkoutCommand.call();
                log.info("Checkout branch: {}", haloBranch.getName());
            }

            // locate theme property location
            var themePropertyPath = ThemeMetaLocator.INSTANCE.locateProperty(tempDirectory)
                .orElseThrow(() -> new ThemePropertyMissingException("主题配置文件缺失，请确认后重试！"));

            // fetch property
            return ThemePropertyScanner.INSTANCE.fetchThemeProperty(themePropertyPath.getParent())
                .orElseThrow();
        } catch (IOException | GitAPIException e) {
            throw new RuntimeException("主题拉取失败！（" + e.getMessage() + "）", e);
        }
    }

}
