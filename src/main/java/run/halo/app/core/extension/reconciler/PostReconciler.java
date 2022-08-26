package run.halo.app.core.extension.reconciler;

import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.springframework.util.Assert;
import run.halo.app.content.ContentService;
import run.halo.app.core.extension.Post;
import run.halo.app.core.extension.Snapshot;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.controller.Reconciler.Request;
import run.halo.app.infra.Condition;
import run.halo.app.infra.ConditionStatus;
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
public class PostReconciler implements Reconciler<Request> {
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
        } else {
            status.setExcerpt(excerpt.getRaw());
        }

        // handle contributors
        String headSnapshot = post.getSpec().getHeadSnapshot();
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

                // update in progress status
                snapshots.stream()
                    .filter(snapshot -> snapshot.getMetadata().getName().equals(headSnapshot))
                    .findAny()
                    .ifPresent(snapshot -> {
                        status.setInProgress(!isPublished(snapshot));
                    });
            });

        // handle cancel publish,has released version and published is false and not handled
        if (StringUtils.isNotBlank(spec.getReleaseSnapshot())
            && Objects.equals(false, spec.getPublished())
            && !StringUtils.equals(status.getPhase(), Post.PostPhase.DRAFT.name())) {
            Condition condition = new Condition();
            condition.setType("CancelledPublish");
            condition.setStatus(ConditionStatus.TRUE);
            condition.setReason(condition.getType());
            condition.setMessage(StringUtils.EMPTY);
            condition.setLastTransitionTime(Instant.now());
            status.getConditionsOrDefault().add(condition);
            status.setPhase(Post.PostPhase.DRAFT.name());
        }

        // handle logic delete
        Map<String, String> labels = getLabelsOrDefault(post);
        if (Objects.equals(spec.getDeleted(), true)) {
            labels.put(Post.DELETED_LABEL, Boolean.TRUE.toString());
            // TODO do more about logic delete such as remove router
        } else {
            labels.put(Post.DELETED_LABEL, Boolean.FALSE.toString());
        }
        // synchronize some fields to labels to query
        labels.put(Post.PHASE_LABEL, status.getPhase());
        labels.put(Post.VISIBLE_LABEL,
            Objects.requireNonNullElse(spec.getVisible(), Post.VisibleEnum.PUBLIC).name());
        labels.put(Post.OWNER_LABEL, spec.getOwner());
    }

    private Map<String, String> getLabelsOrDefault(Post post) {
        Assert.notNull(post, "The post must not be null.");
        Map<String, String> labels = post.getMetadata().getLabels();
        if (labels == null) {
            labels = new LinkedHashMap<>();
            post.getMetadata().setLabels(labels);
        }
        return labels;
    }

    private String getExcerpt(String htmlContent) {
        String shortHtmlContent = StringUtils.substring(htmlContent, 0, 500);
        String text = Jsoup.parse(shortHtmlContent).text();
        // TODO The default capture 150 words as excerpt
        return StringUtils.substring(text, 0, 150);
    }

    private boolean isPublished(Snapshot snapshot) {
        return snapshot.getSpec().getPublishTime() != null;
    }
}
