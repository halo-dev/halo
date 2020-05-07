package run.halo.app.utils;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import org.junit.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Git test.
 *
 * @author johnniang
 * @date 19-5-21
 */
@Slf4j
public class GitTest {

    private Path tempPath;

    @Before
    public void setUp() throws IOException {
        tempPath = Files.createTempDirectory("git-test");
    }

    @After
    public void destroy() throws IOException {
        FileUtils.deleteFolder(tempPath);
    }

    @Test(expected = RepositoryNotFoundException.class)
    public void openFailureTest() throws IOException {
        Git.open(tempPath.toFile());
    }

    @Test
    public void initTest() throws GitAPIException {
        Git.init().setDirectory(tempPath.toFile()).call();
    }

    @Test
    public void statusSuccessfulTest() throws GitAPIException {
        Git git = Git.init().setDirectory(tempPath.toFile()).call();
        Status status = git.status().call();
        log.debug("Status missing: [{}]", status.getMissing());
    }

    @Test
    public void remoteAddTest() throws GitAPIException, URISyntaxException {
        Git git = Git.init().setDirectory(tempPath.toFile()).call();
        git.remoteRemove().setRemoteName("theme-provider").call();
        git.remoteAdd().setName("theme-provider").setUri(new URIish("https://github.com/halo-dev/halo-theme-pinghsu.git")).call();
        List<RemoteConfig> remoteConfigs = git.remoteList().call();
        remoteConfigs.forEach(remoteConfig -> log.debug("name: [{}], url: [{}]", remoteConfig.getName(), remoteConfig.getURIs()));
    }

    @Test
    @Ignore
    public void cloneTest() throws GitAPIException {
        cloneRepository();
    }

    @Test
    @Ignore
    public void pullTest() throws GitAPIException {
        Git git = cloneRepository();
        git.pull().call();
        git.clean().call();
        git.close();
    }

    @Test
    public void getAllBranchesTest() {
        List<String> branches=GitUtils.getAllBranches("https://github.com/halo-dev/halo-theme-hux.git");
        Assert.assertNotNull(branches);
    }

    @Test
    public void getLatestReleaseTest() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        String zipUri = GitUtils.getLastestRelease("https://github.com/halo-dev/halo-theme-hux");
        Assert.assertNotNull(zipUri);
    }

    @Test
    public void accessThemePropertyTest() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, IOException {
        String themeProperty = GitUtils.accessThemeProperty("https://github.com/halo-dev/halo-theme-hux");
        Assert.assertNotNull(themeProperty);
    }

    private Git cloneRepository() throws GitAPIException {
        return Git.cloneRepository()
            .setURI("https://github.com/halo-dev/halo-theme-pinghsu.git")
            .setDirectory(tempPath.toFile())
            .call();
    }
}
