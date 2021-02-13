package run.halo.app.utils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

/**
 * Git utilities.
 *
 * @author johnniang
 * @date 19-6-12
 */
@Slf4j
public class GitUtils {

    private GitUtils() {
    }

    @Deprecated(since = "1.4.2", forRemoval = true)
    public static void cloneFromGit(@NonNull String repoUrl, @NonNull Path targetPath,
        @NonNull String branchName) throws GitAPIException {
        Assert.hasText(repoUrl, "Repository remote url must not be blank");
        Assert.notNull(targetPath, "Target path must not be null");

        try (
            Git ignored = Git.cloneRepository()
                .setURI(repoUrl)
                .setDirectory(targetPath.toFile())
                .setBranchesToClone(Collections.singletonList("refs/heads/" + branchName))
                .setCloneSubmodules(true)
                .setBranch("refs/heads/" + branchName)
                .call()) {
            // empty block placeholder
        }
    }

    public static List<String> getAllBranchesFromRemote(@NonNull String repoUrl) {
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

    @Nullable
    public static Pair<Ref, RevCommit> getLatestTag(final Git git)
        throws GitAPIException, IOException {
        final var tags = git.tagList().call();
        if (CollectionUtils.isEmpty(tags)) {
            return null;
        }
        try (final var revWalk = new RevWalk(git.getRepository())) {
            revWalk.reset();
            revWalk.setTreeFilter(TreeFilter.ANY_DIFF);
            revWalk.sort(RevSort.TOPO, true);
            revWalk.sort(RevSort.COMMIT_TIME_DESC, true);

            final var commitTagMap = new HashMap<RevCommit, Ref>(tags.size());

            for (final var tag : tags) {
                final var commit = revWalk.parseCommit(tag.getObjectId());
                commitTagMap.put(commit, tag);
                if (log.isDebugEnabled()) {
                    log.debug("tag: {} with commit: {} {}", tag.getName(),
                        commit.getFullMessage(), new Date(commit.getCommitTime() * 1000L));
                }
            }

            return commitTagMap.keySet()
                .stream()
                .max(Comparator.comparing(RevCommit::getCommitTime))
                .map(latestCommit -> Pair.of(commitTagMap.get(latestCommit), latestCommit))
                .orElse(null);
        }
    }

    public static void removeRemoteIfExists(final Git git, String remote) throws GitAPIException {
        final var remoteExists = git.remoteList()
            .call()
            .stream().map(RemoteConfig::getName)
            .anyMatch(name -> name.equals(remote));
        if (remoteExists) {
            // remove newRepo remote
            final var removedRemoteConfig = git.remoteRemove()
                .setRemoteName(remote)
                .call();
            log.info("git remote remove {} {}", removedRemoteConfig.getName(),
                removedRemoteConfig.getURIs());
        }
    }

    public static void logCommit(final RevCommit commit) {
        if (commit == null) {
            return;
        }
        log.info("Commit result: {} {} {}",
            commit.getName(),
            commit.getFullMessage(),
            new Date(commit.getCommitTime() * 1000L));
    }

    public static void commitAutomatically(final Git git) throws GitAPIException, IOException {
        // git status
        if (git.status().call().isClean()) {
            final var branch = git.getRepository().getBranch();
            final var fullBranch = git.getRepository().getFullBranch();
            log.info("Current branch {}", branch);
            log.info("Your branch is up to date with {}.", fullBranch);
            log.info("");
            log.info("nothing to commit, working tree clean");
            return;
        }
        // git add .
        git.add().addFilepattern(".").call();
        log.info("git add .");
        // git commit -m "Committed by halo automatically."
        final var commit = git.commit()
            .setSign(false)
            .setAuthor("halo", "hi@halo.run")
            .setMessage("Committed by halo automatically.")
            .call();
        log.info("git commit -m \"Committed by halo automatically.\"");
        logCommit(commit);
    }
}
