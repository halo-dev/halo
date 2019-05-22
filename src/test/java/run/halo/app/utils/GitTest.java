package run.halo.app.utils;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Git test.
 *
 * @author johnniang
 * @date 19-5-21
 */
@Ignore
public class GitTest {

    private final Path tempPath;

    public GitTest() throws IOException {
        tempPath = Files.createTempDirectory("git-test");
    }

    @Test(expected = RepositoryNotFoundException.class)
    public void openTest() throws IOException {
        Git.open(tempPath.toFile());
    }

    @Test
    public void initTest() throws GitAPIException {
        Git.init().setDirectory(tempPath.toFile()).call();
    }

    @Test
    public void statusTest() throws GitAPIException {
        Git git = Git.init().setDirectory(tempPath.toFile()).call();
        git.status().call();
    }

    @Test
    public void cloneTest() throws GitAPIException {
        cloneRepository();
    }

    @Test
    public void pullTest() throws GitAPIException {
        Git git = cloneRepository();
        git.pull().call();
        git.clean().call();
        git.close();
    }

    private Git cloneRepository() throws GitAPIException {
        return Git.cloneRepository()
                .setURI("https://github.com/halo-dev/halo-theme-pinghsu.git")
                .setDirectory(tempPath.toFile())
                .call();
    }
}
