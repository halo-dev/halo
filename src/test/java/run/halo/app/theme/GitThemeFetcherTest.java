package run.halo.app.theme;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import run.halo.app.utils.FileUtils;

/**
 * Git theme fetcher test.
 *
 * @author johnniang
 */
@Slf4j
class GitThemeFetcherTest {

    GitThemeFetcher gitThemeFetcher;

    @BeforeEach
    void setUp() {
        this.gitThemeFetcher = new GitThemeFetcher();
    }

    @Test
    @Disabled("Time consumption")
    void fetchTest() throws IOException, GitAPIException {
        final var repo = "https://gitee.com/xzhuz/halo-theme-xue";
        final var property = this.gitThemeFetcher.fetch(repo);
        final var themePath = Paths.get(property.getThemePath());
        try (final var git = Git.open(themePath.toFile())) {
            final var remoteConfigs = git.remoteList().call();
            assertEquals(1, remoteConfigs.size());
            assertEquals("upstream", remoteConfigs.get(0).getName());

            List<Ref> refs = git.branchList().call();
            assertEquals(2, refs.size());
            assertEquals("refs/heads/halo", refs.get(0).getName());
        }

        Path tempDirectory = FileUtils.createTempDirectory();
        // copy repo to temp folder
        FileUtils.copyFolder(themePath, tempDirectory);
        try (final var git = Git.open(tempDirectory.toFile())) {
            final var remoteConfigs = git.remoteList().call();
            assertEquals(1, remoteConfigs.size());
            assertEquals("upstream", remoteConfigs.get(0).getName());

            List<Ref> refs = git.branchList().call();
            assertEquals(2, refs.size());
            assertEquals("refs/heads/halo", refs.get(0).getName());
        }
    }
}