import com.spotify.github.v3.clients.GitDataClient;
import com.spotify.github.v3.clients.GitHubClient;
import com.spotify.github.v3.clients.GithubAppClient;
import com.spotify.github.v3.clients.RepositoryClient;
import com.spotify.github.v3.git.Tag;
import com.spotify.github.v3.repos.Repository;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutionException;
import org.kohsuke.github.GHAsset;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.PagedIterable;

public class Test {

    public static void testGithubClient() throws ExecutionException, InterruptedException {
        final GitHubClient gitHubClient = GitHubClient.create(URI.create("https://api.github.com"),
            "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");
        final RepositoryClient halo = gitHubClient.createRepositoryClient("Camsyn", "halo");
        System.out.println(halo.getRepository().get());
        final GitDataClient gitDataClient = gitHubClient.createGitDataClient("Camsyn", "halo");
        final Repository repository = halo.getRepository().get();
        System.out.println(repository.releasesUrl());
        System.out.println(repository.downloadsUrl());


//        final Tag tag = gitDataClient.getTag("v1.5.3").get();
//        System.out.println(tag);

    }

    public static void main(String[] args)
        throws IOException, ExecutionException, InterruptedException {
//        testGithubClient();
        testGithubAPI();
    }

    private static void testGithubAPI() throws IOException {
        final GitHub github = new GitHubBuilder().build();
        final GHRepository repo = github.getRepository("halo-dev/halo");
        System.out.println(github.getApiUrl());
        final GHRelease release = repo.getReleaseByTagName("v1.5.3");
        final PagedIterable<GHAsset> assets = release.listAssets();
        System.out.println(assets.toList().get(0).getBrowserDownloadUrl());
//        System.out.println(github.getApiUrl());

    }
}
