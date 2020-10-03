package run.halo.app.utils;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Git test.
 *
 * @author johnniang
 * @date 19-5-21
 */
@Slf4j
class GitTest {

    Path tempPath;

    @BeforeEach
    void setUp() throws IOException {
        tempPath = Files.createTempDirectory("git-test");
    }

    @AfterEach
    void destroy() throws IOException {
        FileUtils.deleteFolder(tempPath);
    }

    @Test
    void openFailureTest() {
        Assertions.assertThrows(RepositoryNotFoundException.class, () -> Git.open(tempPath.toFile()));
    }

    @Test
    void initTest() throws GitAPIException {
        Git.init().setDirectory(tempPath.toFile()).call();
    }

    @Test
    void statusSuccessfulTest() throws GitAPIException {
        Git git = Git.init().setDirectory(tempPath.toFile()).call();
        Status status = git.status().call();
        log.debug("Status missing: [{}]", status.getMissing());
    }

    @Test
    void remoteAddTest() throws GitAPIException, URISyntaxException {
        Git git = Git.init().setDirectory(tempPath.toFile()).call();
        git.remoteRemove().setRemoteName("theme-provider").call();
        git.remoteAdd().setName("theme-provider").setUri(new URIish("https://github.com/halo-dev/halo-theme-pinghsu.git")).call();
        List<RemoteConfig> remoteConfigs = git.remoteList().call();
        remoteConfigs.forEach(remoteConfig -> log.debug("name: [{}], url: [{}]", remoteConfig.getName(), remoteConfig.getURIs()));
    }

    @Test
    @Disabled("Due to time-consumption cloning")
    void cloneTest() throws GitAPIException {
        cloneRepository();
    }

    @Test
    @Disabled("Due to time-consumption cloning and pulling")
    void pullTest() throws GitAPIException {
        Git git = cloneRepository();
        git.pull().call();
        git.clean().call();
        git.close();
    }

    @Test
    @Disabled("Due to time-consumption fetching")
    void getAllBranchesTest() {
        List<String> branches = GitUtils.getAllBranches("https://github.com/halo-dev/halo-theme-hux.git");
        assertNotNull(branches);
    }

    @Test
    @Disabled("Due to time-consumption fetching")
    void getAllBranchesWithInvalidURL() {
        List<String> branches = GitUtils.getAllBranches("https://github.com/halo-dev/halo-theme.git");
        assertNotNull(branches);
        assertEquals(0, branches.size());
    }

    Git cloneRepository() throws GitAPIException {
        return Git.cloneRepository()
                .setURI("https://github.com/halo-dev/halo-theme-pinghsu.git")
                .setDirectory(tempPath.toFile())
                .call();
    }
}
