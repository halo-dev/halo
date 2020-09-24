package run.halo.app.utils;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.storage.file.WindowCacheConfig;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Git utilities.
 *
 * @author johnniang
 * @date 19-6-12
 */
@Slf4j
public class GitUtils {

    private GitUtils() {
        // Config packed git MMAP
        WindowCacheConfig config = new WindowCacheConfig();
        config.setPackedGitMMAP(false);
        config.install();
    }

    public static void cloneFromGit(@NonNull String repoUrl, @NonNull Path targetPath) throws GitAPIException {
        Assert.hasText(repoUrl, "Repository remote url must not be blank");
        Assert.notNull(targetPath, "Target path must not be null");

        log.debug("Trying to clone git repo [{}] to [{}]", repoUrl, targetPath);

        // Use try-with-resource-statement
        Git git = null;
        try {
            git = Git.cloneRepository()
                    .setURI(repoUrl)
                    .setDirectory(targetPath.toFile())
                    .call();
            log.debug("Cloned git repo [{}] successfully", repoUrl);
        } finally {
            closeQuietly(git);
        }
    }

    public static Git openOrInit(Path repoPath) throws IOException, GitAPIException {
        Git git;

        try {
            git = Git.open(repoPath.toFile());
        } catch (RepositoryNotFoundException e) {
            log.warn("Git repository may not exist, we will try to initialize an empty repository: [{}]", e.getMessage());
            git = Git.init().setDirectory(repoPath.toFile()).call();
        }

        return git;
    }

    public static void cloneFromGit(@NonNull String repoUrl, @NonNull Path targetPath, @NonNull String branchName) throws GitAPIException {
        Assert.hasText(repoUrl, "Repository remote url must not be blank");
        Assert.notNull(targetPath, "Target path must not be null");

        Git git = null;
        try {
            git = Git.cloneRepository()
                    .setURI(repoUrl)
                    .setDirectory(targetPath.toFile())
                    .setBranchesToClone(Collections.singletonList("refs/heads/" + branchName))
                    .setBranch("refs/heads/" + branchName)
                    .call();
        } finally {
            closeQuietly(git);
        }
    }

    public static List<String> getAllBranches(@NonNull String repoUrl) {
        List<String> branches = new ArrayList<>();
        try {
            Collection<Ref> refs = Git.lsRemoteRepository()
                    .setHeads(true)
                    .setRemote(repoUrl)
                    .call();
            for (Ref ref : refs) {
                branches.add(ref.getName().substring(ref.getName().lastIndexOf("/") + 1));
            }
        } catch (InvalidRemoteException e) {
            log.warn("Git url is not valid: [{}]", e.getMessage());
        } catch (TransportException e) {
            log.warn("Transport exception: [{}]", e.getMessage());
        } catch (GitAPIException e) {
            log.warn("Git api exception: [{}]", e.getMessage());
        }
        return branches;
    }

    public static void closeQuietly(Git git) {
        if (git != null) {
            git.getRepository().close();
            git.close();
        }
    }

}
