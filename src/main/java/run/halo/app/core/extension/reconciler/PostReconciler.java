package run.halo.app.core.extension.reconciler;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import run.halo.app.content.ContentService;
import run.halo.app.core.extension.Post;
import run.halo.app.core.extension.Snapshot;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.infra.utils.JsonUtils;

/**
 * <p>Reconciler for {@link Post}.</p>
 *
 * <p>things to do:</p>
 * <ul>
 * 1. generate permalink
 * 2. generate excerpt if auto generate is enabled
 * </ul>
 *
 * @author guqing
 * @since 2.0.0
 */
public class PostReconciler implements Reconciler {
    public static final String PERMALINK_PREFIX = "/permalink/posts/";
    private final ExtensionClient client;
    private final ContentService contentService;

    public PostReconciler(ExtensionClient client, ContentService contentService) {
        this.client = client;
        this.contentService = contentService;
    }

    @Override
    public Result reconcile(Request request) {
        client.fetch(Post.class, request.name())
            .ifPresent(post -> {
                Post oldPost = JsonUtils.deepCopy(post);

                doReconcile(post);

                if (!oldPost.equals(post)) {
                    client.update(post);
                }
            });
        return new Result(false, null);
    }

    private void doReconcile(Post post) {
        String name = post.getMetadata().getName();
        Post.PostSpec spec = post.getSpec();
        Post.PostStatus status = post.getStatusOrDefault();
        if (status.getPhase() == null) {
            status.setPhase(Post.PostPhase.DRAFT.name());
        }
        // handle permalink
        if (StringUtils.isBlank(status.getPermalink())) {
            status.setPermalink(PERMALINK_PREFIX + name);
        }

        // handle excerpt
        Post.Excerpt excerpt = spec.getExcerpt();
        if (excerpt == null) {
            excerpt = new Post.Excerpt();
            excerpt.setAutoGenerate(true);
            spec.setExcerpt(excerpt);
        }

        if (excerpt.getAutoGenerate()) {
            contentService.getContent(spec.getHeadSnapshot())
                .subscribe(content -> {
                    String contentRevised = content.content();
                    status.setExcerpt(getExcerpt(contentRevised));
                });
        }

        // handle contributors
        contentService.listSnapshots(Snapshot.SubjectRef.of(Post.KIND, name))
            .collectList()
            .subscribe(snapshots -> {
                List<String> contributors = snapshots.stream()
                    .map(snapshot -> {
                        Set<String> usernames = snapshot.getSpec().getContributors();
                        return Objects.requireNonNullElseGet(usernames,
                            () -> new HashSet<String>());
                    })
                    .flatMap(Set::stream)
                    .distinct()
                    .sorted()
                    .toList();
                status.setContributors(contributors);
            });
    }

    private String getExcerpt(String htmlContent) {
        String shortHtmlContent = StringUtils.substring(htmlContent, 0, 500);
        String text = Jsoup.parse(shortHtmlContent).text();
        // TODO The default capture 150 words as excerpt
        return StringUtils.substring(text, 0, 150);
    }
}
