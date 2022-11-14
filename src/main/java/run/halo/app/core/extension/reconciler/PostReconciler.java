package run.halo.app.core.extension.reconciler;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;
import run.halo.app.content.ContentService;
import run.halo.app.content.PostService;
import run.halo.app.content.permalinks.PostPermalinkPolicy;
import run.halo.app.core.extension.Comment;
import run.halo.app.core.extension.Post;
import run.halo.app.core.extension.Snapshot;
import run.halo.app.event.post.PostUnpublishedEvent;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ExtensionOperator;
import run.halo.app.extension.ExtensionUtil;
import run.halo.app.extension.Ref;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.infra.Condition;
import run.halo.app.infra.ConditionList;
import run.halo.app.infra.ConditionStatus;
import run.halo.app.infra.utils.HaloUtils;
import run.halo.app.infra.utils.JsonUtils;
import run.halo.app.metrics.CounterService;
import run.halo.app.metrics.MeterUtils;

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
@AllArgsConstructor
public class PostReconciler implements Reconciler<Reconciler.Request> {
    private static final String FINALIZER_NAME = "post-protection";
    private final ExtensionClient client;
    private final ContentService contentService;
    private final PostService postService;
    private final PostPermalinkPolicy postPermalinkPolicy;
    private final CounterService counterService;

    private final ApplicationContext applicationContext;

    @Override
    public Result reconcile(Request request) {
        client.fetch(Post.class, request.name())
            .ifPresent(post -> {
                if (ExtensionOperator.isDeleted(post)) {
                    cleanUpResourcesAndRemoveFinalizer(request.name());
                    return;
                }
                addFinalizerIfNecessary(post);

                // reconcile spec first
                reconcileSpec(request.name());
                reconcileMetadata(request.name());
                reconcileStatus(request.name());
            });
        return new Result(false, null);
    }

    private void reconcileSpec(String name) {
        client.fetch(Post.class, name).ifPresent(post -> {
            // un-publish post if necessary
            if (post.isPublished()
                && Objects.equals(false, post.getSpec().getPublish())) {
                boolean success = unPublishReconcile(name);
                if (success) {
                    applicationContext.publishEvent(new PostUnpublishedEvent(this, name));
                }
                return;
            }

            // publish post if necessary
            try {
                postService.publishPost(name, post.getSpec().getReleaseSnapshot()).block();
            } catch (Throwable e) {
                publishFailed(name, e);
                throw e;
            }
        });
    }

    private boolean unPublishReconcile(String name) {
        return client.fetch(Post.class, name)
            .map(post -> {
                final Post oldPost = JsonUtils.deepCopy(post);
                Post.changePublishedState(post, false);
                final Post.PostStatus status = post.getStatusOrDefault();
                Condition condition = new Condition();
                condition.setType("CancelledPublish");
                condition.setStatus(ConditionStatus.TRUE);
                condition.setReason(condition.getType());
                condition.setMessage("CancelledPublish");
                condition.setLastTransitionTime(Instant.now());
                status.getConditionsOrDefault().add(condition);
                status.setPhase(Post.PostPhase.DRAFT.name());
                if (!oldPost.equals(post)) {
                    client.update(post);
                }
                return true;
            })
            .orElse(false);
    }

    private void publishFailed(String name, Throwable error) {
        Assert.notNull(name, "Name must not be null");
        Assert.notNull(error, "Error must not be null");
        client.fetch(Post.class, name).ifPresent(post -> {
            final Post oldPost = JsonUtils.deepCopy(post);

            Post.PostStatus status = post.getStatusOrDefault();
            Post.PostPhase phase = Post.PostPhase.FAILED;
            status.setPhase(phase.name());

            final ConditionList conditions = status.getConditionsOrDefault();
            Condition condition = new Condition();
            condition.setType(phase.name());
            condition.setReason(phase.name());
            condition.setMessage("");
            condition.setStatus(ConditionStatus.TRUE);
            condition.setLastTransitionTime(Instant.now());
            condition.setMessage(error.getMessage());
            condition.setStatus(ConditionStatus.FALSE);
            conditions.addAndEvictFIFO(condition);

            post.setStatus(status);

            if (!oldPost.equals(post)) {
                client.update(post);
            }
        });
    }

    private void reconcileMetadata(String name) {
        client.fetch(Post.class, name).ifPresent(post -> {
            final Post oldPost = JsonUtils.deepCopy(post);
            Post.PostSpec spec = post.getSpec();

            // handle logic delete
            Map<String, String> labels = ExtensionUtil.nullSafeLabels(post);
            if (Objects.equals(spec.getDeleted(), true)) {
                labels.put(Post.DELETED_LABEL, Boolean.TRUE.toString());
            } else {
                labels.put(Post.DELETED_LABEL, Boolean.FALSE.toString());
            }
            labels.put(Post.VISIBLE_LABEL,
                Objects.requireNonNullElse(spec.getVisible(), Post.VisibleEnum.PUBLIC).name());
            labels.put(Post.OWNER_LABEL, spec.getOwner());
            Instant publishTime = post.getSpec().getPublishTime();
            if (publishTime != null) {
                labels.put(Post.ARCHIVE_YEAR_LABEL, HaloUtils.getYearText(publishTime));
                labels.put(Post.ARCHIVE_MONTH_LABEL, HaloUtils.getMonthText(publishTime));
            }
            if (!labels.containsKey(Post.PUBLISHED_LABEL)) {
                labels.put(Post.PUBLISHED_LABEL, Boolean.FALSE.toString());
            }
            if (!oldPost.equals(post)) {
                client.update(post);
            }
        });
    }

    private void reconcileStatus(String name) {
        client.fetch(Post.class, name).ifPresent(post -> {
            final Post oldPost = JsonUtils.deepCopy(post);
            postPermalinkPolicy.onPermalinkDelete(oldPost);

            post.getStatusOrDefault()
                .setPermalink(postPermalinkPolicy.permalink(post));
            postPermalinkPolicy.onPermalinkAdd(post);

            Post.PostStatus status = post.getStatusOrDefault();
            if (status.getPhase() == null) {
                status.setPhase(Post.PostPhase.DRAFT.name());
            }
            Post.PostSpec spec = post.getSpec();
            // handle excerpt
            Post.Excerpt excerpt = spec.getExcerpt();
            if (excerpt == null) {
                excerpt = new Post.Excerpt();
                excerpt.setAutoGenerate(true);
                spec.setExcerpt(excerpt);
            }
            if (excerpt.getAutoGenerate()) {
                contentService.getContent(spec.getReleaseSnapshot())
                    .blockOptional()
                    .ifPresent(content -> {
                        String contentRevised = content.getContent();
                        status.setExcerpt(getExcerpt(contentRevised));
                    });
            } else {
                status.setExcerpt(excerpt.getRaw());
            }

            Ref ref = Ref.of(post);
            // handle contributors
            String headSnapshot = post.getSpec().getHeadSnapshot();
            contentService.listSnapshots(ref)
                .collectList()
                .blockOptional()
                .ifPresent(snapshots -> {
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
                    status.setInProgress(
                        !StringUtils.equals(headSnapshot, post.getSpec().getReleaseSnapshot()));
                });

            if (!oldPost.equals(post)) {
                client.update(post);
            }
        });
    }

    private void addFinalizerIfNecessary(Post oldPost) {
        Set<String> finalizers = oldPost.getMetadata().getFinalizers();
        if (finalizers != null && finalizers.contains(FINALIZER_NAME)) {
            return;
        }
        client.fetch(Post.class, oldPost.getMetadata().getName())
            .ifPresent(post -> {
                Set<String> newFinalizers = post.getMetadata().getFinalizers();
                if (newFinalizers == null) {
                    newFinalizers = new HashSet<>();
                    post.getMetadata().setFinalizers(newFinalizers);
                }
                newFinalizers.add(FINALIZER_NAME);
                client.update(post);
            });
    }

    private void cleanUpResourcesAndRemoveFinalizer(String postName) {
        client.fetch(Post.class, postName).ifPresent(post -> {
            cleanUpResources(post);
            if (post.getMetadata().getFinalizers() != null) {
                post.getMetadata().getFinalizers().remove(FINALIZER_NAME);
            }
            client.update(post);
        });
    }

    private void cleanUpResources(Post post) {
        // remove permalink from permalink indexer
        postPermalinkPolicy.onPermalinkDelete(post);

        // clean up snapshots
        final Ref ref = Ref.of(post);
        client.list(Snapshot.class,
                snapshot -> ref.equals(snapshot.getSpec().getSubjectRef()), null)
            .forEach(client::delete);

        // clean up comments
        client.list(Comment.class, comment -> comment.getSpec().getSubjectRef().equals(ref),
                null)
            .forEach(client::delete);

        // delete counter
        counterService.deleteByName(MeterUtils.nameOf(Post.class, post.getMetadata().getName()))
            .block();
    }

    private String getExcerpt(String htmlContent) {
        String shortHtmlContent = StringUtils.substring(htmlContent, 0, 500);
        String text = Jsoup.parse(shortHtmlContent).text();
        // TODO The default capture 150 words as excerpt
        return StringUtils.substring(text, 0, 150);
    }
}
