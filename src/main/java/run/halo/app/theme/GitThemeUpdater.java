package run.halo.app.theme;

import static run.halo.app.theme.ThemeUpdater.backup;
import static run.halo.app.theme.ThemeUpdater.restore;
import static run.halo.app.utils.GitUtils.commitAutomatically;
import static run.halo.app.utils.GitUtils.logCommit;
import static run.halo.app.utils.GitUtils.removeRemoteIfExists;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RebaseCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.RepositoryState;
import org.eclipse.jgit.transport.URIish;
import run.halo.app.exception.NotFoundException;
import run.halo.app.exception.ServiceException;
import run.halo.app.exception.ThemeUpdateException;
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

    public GitThemeUpdater(ThemeRepository themeRepository,
        ThemeFetcherComposite fetcherComposite) {
        this.themeRepository = themeRepository;
        this.fetcherComposite = fetcherComposite;
    }

    @Override
    public ThemeProperty update(String themeId) throws IOException {
        // get theme property
        final var oldThemeProperty = themeRepository.fetchThemePropertyByThemeId(themeId)
            .orElseThrow(
                () -> new NotFoundException("主题 " + themeId + " 不存在或已删除！").setErrorData(themeId));

        // get update config
        final var gitRepo = oldThemeProperty.getRepo();

        // fetch latest theme
        final var newThemeProperty = fetcherComposite.fetch(gitRepo);

        // merge old theme and new theme
        final var mergedThemeProperty = merge(oldThemeProperty, newThemeProperty);

        // backup old theme
        final var backupPath = backup(oldThemeProperty);

        try {
            // delete old theme
            themeRepository.deleteTheme(oldThemeProperty);

            // copy new theme to old theme folder
            return themeRepository.attemptToAdd(mergedThemeProperty);
        } catch (Throwable t) {
            log.error("Failed to add new theme, and restoring old theme from " + backupPath, t);
            // restore old theme
            restore(backupPath, oldThemeProperty);
            log.info("Restored old theme from path: {}", backupPath);
            throw t;
        }
    }

    public ThemeProperty merge(ThemeProperty oldThemeProperty, ThemeProperty newThemeProperty)
        throws IOException {

        final var oldThemePath = Paths.get(oldThemeProperty.getThemePath());
        // open old git repo
        try (final var oldGit = Git.init().setDirectory(oldThemePath.toFile()).call()) {
            // 0. commit old repo
            commitAutomatically(oldGit);

            final var newThemePath = Paths.get(newThemeProperty.getThemePath());
            // trying to open new git repo
            try (final var ignored = Git.open(newThemePath.toFile())) {
                // remove remote
                removeRemoteIfExists(oldGit, "newTheme");
                // add this new git to remote for old repo
                final var addedRemoteConfig = oldGit.remoteAdd()
                    .setName("newTheme")
                    .setUri(new URIish(newThemePath.toString()))
                    .call();
                log.info("git remote add newTheme {} {}",
                    addedRemoteConfig.getName(),
                    addedRemoteConfig.getURIs());

                // fetch remote data
                final var remote = "newTheme/halo";
                log.info("git fetch newTheme/halo");
                final var fetchResult = oldGit.fetch()
                    .setRemote("newTheme")
                    .call();
                log.info("Fetch result: {}", fetchResult.getMessages());

                // rebase upstream
                log.info("git rebase newTheme");
                final var rebaseResult = oldGit.rebase()
                    .setUpstream(remote)
                    .call();
                log.info("Rebase result: {}", rebaseResult.getStatus());
                logCommit(rebaseResult.getCurrentCommit());

                // check rebase result
                if (!rebaseResult.getStatus().isSuccessful()) {
                    if (oldGit.getRepository().getRepositoryState() != RepositoryState.SAFE) {
                        // if rebasing stopped or failed, you can get back to the original state by
                        // running it
                        // with setOperation(RebaseCommand.Operation.ABORT)
                        final var abortRebaseResult = oldGit.rebase()
                            .setUpstream(remote)
                            .setOperation(RebaseCommand.Operation.ABORT)
                            .call();
                        log.error("Aborted rebase with state: {} : {}",
                            abortRebaseResult.getStatus(),
                            abortRebaseResult.getConflicts());
                    }
                    throw new ThemeUpdateException("无法自动合并最新文件！请尝试删除主题并重新拉取。");
                }
            }
        } catch (URISyntaxException | GitAPIException e) {
            throw new ServiceException("合并主题失败！请确认该主题支持在线更新。", e);
        }

        return newThemeProperty;
    }

}
