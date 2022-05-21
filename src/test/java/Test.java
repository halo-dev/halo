import java.io.IOException;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

public class Test {
    public static void main(String[] args) throws IOException {
        final GitHub github = GitHub.connect();
        final GHRepository repo = github.createRepository("halo").owner("halo-dev")
            .homepage("https://github.com/halo-dev/halo").create();
        System.out.println(repo.getReleases());
    }
}
