package run.halo.app.theme;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.TagOpt;
import org.eclipse.jgit.transport.URIish;
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

    public ThemeProperty merge(ThemeProperty oldThemeProperty, ThemeProperty newThemeProperty) throws IOException {
        //TODO Complete merging process
        // make sure that both themes contain .git folder
        final var oldThemePath = Paths.get(oldThemeProperty.getThemePath());
        final var newThemePath = Paths.get(newThemeProperty.getThemePath());
        // open old git repo
        try (final var oldGit = Git.open(oldThemePath.toFile())) {
            // try to add and commit changes not staged
            if (!oldGit.status().call().isClean()) {
                oldGit.add().addFilepattern(".").call();
                oldGit.commit()
                        .setSign(false)
                        .setAuthor("halo", "hi@halo.run")
                        .setMessage("Committed by halo automatically.")
                        .call();
            }
            // open new git repo
            try (final var newGit = Git.open(newThemePath.toFile())) {
                newGit.fetch().setTagOpt(TagOpt.FETCH_TAGS).call();
                // clear remote
                final var remoteExists = oldGit.remoteList()
                        .call()
                        .stream().map(RemoteConfig::getName)
                        .anyMatch(name -> name.equalsIgnoreCase("newRepo"));
                if (remoteExists) {
                    // remove newRepo remote
                    oldGit.remoteRemove()
                            .setRemoteName("newRepo")
                            .call();
                }
                // add this new git to remote for old repo
                oldGit.remoteAdd()
                        .setName("newRepo")
                        .setUri(new URIish(newThemePath.toString()))
                        .call();
                // fetch remote data
                final var branch = oldThemeProperty.getBranch();
                oldGit.fetch()
                        .setRemote("newRepo/master")
                        .call();

                oldGit.rebase()
                        .setUpstream("newRepo/master");
            }
        } catch (NoFilepatternException | URISyntaxException e) {
            e.printStackTrace();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }

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
