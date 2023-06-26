package run.halo.app.core.extension.reconciler;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static run.halo.app.extension.ExtensionUtil.addFinalizers;
import static run.halo.app.extension.ExtensionUtil.removeFinalizers;

import com.google.common.hash.Hashing;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import run.halo.app.content.ContentWrapper;
import run.halo.app.content.PostService;
import run.halo.app.content.permalinks.PostPermalinkPolicy;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.core.extension.content.Constant;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.content.Post.PostPhase;
import run.halo.app.core.extension.content.Post.VisibleEnum;
import run.halo.app.core.extension.content.Snapshot;
import run.halo.app.event.post.PostPublishedEvent;
import run.halo.app.event.post.PostUnpublishedEvent;
import run.halo.app.event.post.PostUpdatedEvent;
import run.halo.app.event.post.PostVisibleChangedEvent;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ExtensionOperator;
import run.halo.app.extension.Ref;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.infra.Condition;
import run.halo.app.infra.ConditionStatus;
import run.halo.app.infra.utils.HaloUtils;
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
@Component
public class PostReconciler implements Reconciler<Reconciler.Request> {
    private static final String FINALIZER_NAME = "post-protection";
    private final ExtensionClient client;
    private final PostService postService;
    private final PostPermalinkPolicy postPermalinkPolicy;
    private final CounterService counterService;

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Result reconcile(Request request) {
        var events = new HashSet<ApplicationEvent>();
        client.fetch(Post.class, request.name())
            .ifPresent(post -> {
                if (ExtensionOperator.isDeleted(post)) {
                    removeFinalizers(post.getMetadata(), Set.of(FINALIZER_NAME));
                    unPublishPost(post, events);
                    cleanUpResources(post);
                    // update post to be able to be collected by gc collector.
                    client.update(post);
                    // fire event after updating post
                    events.forEach(eventPublisher::publishEvent);
                    return;
                }
                addFinalizers(post.getMetadata(), Set.of(FINALIZER_NAME));

                var labels = post.getMetadata().getLabels();
                if (labels == null) {
                    labels = new HashMap<>();
                    post.getMetadata().setLabels(labels);
                }

                var annotations = post.getMetadata().getAnnotations();
                if (annotations == null) {
                    annotations = new HashMap<>();
                    post.getMetadata().setAnnotations(annotations);
                }

                var status = post.getStatus();
                if (status == null) {
                    status = new Post.PostStatus();
                    post.setStatus(status);
                }

                // calculate the sha256sum
                var configSha256sum = Hashing.sha256().hashString(post.getSpec().toString(), UTF_8)
                        .toString();

                var oldConfigChecksum = annotations.get(Constant.CHECKSUM_CONFIG_ANNO);
                if (!Objects.equals(oldConfigChecksum, configSha256sum)) {
                    // if the checksum doesn't match
                    events.add(new PostUpdatedEvent(this, post.getMetadata().getName()));
                    annotations.put(Constant.CHECKSUM_CONFIG_ANNO, configSha256sum);
                }

                var expectDelete = defaultIfNull(post.getSpec().getDeleted(), false);
                var expectPublish = defaultIfNull(post.getSpec().getPublish(), false);

                if (expectDelete || !expectPublish) {
                    unPublishPost(post, events);
                } else {
                    publishPost(post, events);
                }

                labels.put(Post.DELETED_LABEL, expectDelete.toString());

                var expectVisible = defaultIfNull(post.getSpec().getVisible(), VisibleEnum.PUBLIC);
                var oldVisible = VisibleEnum.from(labels.get(Post.VISIBLE_LABEL));
                if (!Objects.equals(oldVisible, expectVisible)) {
                    eventPublisher.publishEvent(
                        new PostVisibleChangedEvent(request.name(), oldVisible, expectVisible));
                }
                labels.put(Post.VISIBLE_LABEL, expectVisible.toString());

                var ownerName = post.getSpec().getOwner();
                if (StringUtils.isNotBlank(ownerName)) {
                    labels.put(Post.OWNER_LABEL, ownerName);
                }

                var publishTime = post.getSpec().getPublishTime();
                if (publishTime != null) {
                    labels.put(Post.ARCHIVE_YEAR_LABEL, HaloUtils.getYearText(publishTime));
                    labels.put(Post.ARCHIVE_MONTH_LABEL, HaloUtils.getMonthText(publishTime));
                    labels.put(Post.ARCHIVE_DAY_LABEL, HaloUtils.getDayText(publishTime));
                }

                var permalinkPattern = postPermalinkPolicy.pattern();
                annotations.put(Constant.PERMALINK_PATTERN_ANNO, permalinkPattern);

                status.setPermalink(postPermalinkPolicy.permalink(post));
                if (status.getPhase() == null) {
                    status.setPhase(PostPhase.DRAFT.toString());
                }

                var excerpt = post.getSpec().getExcerpt();
                if (excerpt == null) {
                    excerpt = new Post.Excerpt();
                }
                var isAutoGenerate = defaultIfNull(excerpt.getAutoGenerate(), true);
                if (isAutoGenerate) {
                    Optional<ContentWrapper> contentWrapper =
                        postService.getContent(post.getSpec().getReleaseSnapshot(),
                                post.getSpec().getBaseSnapshot())
                            .blockOptional();
                    if (contentWrapper.isPresent()) {
                        String contentRevised = contentWrapper.get().getContent();
                        status.setExcerpt(getExcerpt(contentRevised));
                    }
                } else {
                    status.setExcerpt(excerpt.getRaw());
                }


                var ref = Ref.of(post);
                // handle contributors
                var headSnapshot = post.getSpec().getHeadSnapshot();
                var contributors = client.list(Snapshot.class,
                        snapshot -> ref.equals(snapshot.getSpec().getSubjectRef()), null)
                    .stream()
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

                client.update(post);
                // fire event after updating post
                events.forEach(eventPublisher::publishEvent);
            });
        return Result.doNotRetry();
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return builder
            .extension(new Post())
            // TODO Make it configurable
            .workerCount(1)
            .build();
    }

    private void publishPost(Post post, Set<ApplicationEvent> events) {
        var expectReleaseSnapshot = post.getSpec().getReleaseSnapshot();
        if (StringUtils.isBlank(expectReleaseSnapshot)) {
            // Do nothing if release snapshot is not set
            return;
        }
        var annotations = post.getMetadata().getAnnotations();
        var lastReleaseSnapshot = annotations.get(Post.LAST_RELEASED_SNAPSHOT_ANNO);
        if (post.isPublished()
            && Objects.equals(expectReleaseSnapshot, lastReleaseSnapshot)) {
            // If the release snapshot is not change
            return;
        }
        var status = post.getStatus();
        // validate the release snapshot
        var snapshot = client.fetch(Snapshot.class, expectReleaseSnapshot);
        if (snapshot.isEmpty()) {
            Condition condition = Condition.builder()
                .type(PostPhase.FAILED.name())
                .reason("SnapshotNotFound")
                .message(
                    String.format("Snapshot [%s] not found for publish", expectReleaseSnapshot))
                .status(ConditionStatus.FALSE)
                .lastTransitionTime(Instant.now())
                .build();
            status.getConditionsOrDefault().addAndEvictFIFO(condition);
            status.setPhase(PostPhase.FAILED.name());
            return;
        }
        annotations.put(Post.LAST_RELEASED_SNAPSHOT_ANNO, expectReleaseSnapshot);
        status.setPhase(PostPhase.PUBLISHED.toString());
        var condition = Condition.builder()
            .type(PostPhase.PUBLISHED.name())
            .reason("Published")
            .message("Post published successfully.")
            .lastTransitionTime(Instant.now())
            .status(ConditionStatus.TRUE)
            .build();
        status.getConditionsOrDefault().addAndEvictFIFO(condition);
        var labels = post.getMetadata().getLabels();
        labels.put(Post.PUBLISHED_LABEL, Boolean.TRUE.toString());
        if (post.getSpec().getPublishTime() == null) {
            // TODO Set the field in creation hook in the future.
            post.getSpec().setPublishTime(Instant.now());
        }
        status.setLastModifyTime(snapshot.get().getSpec().getLastModifyTime());
        events.add(new PostPublishedEvent(this, post.getMetadata().getName()));
    }

    void unPublishPost(Post post, Set<ApplicationEvent> events) {
        if (!post.isPublished()) {
            return;
        }
        var labels = post.getMetadata().getLabels();
        labels.put(Post.PUBLISHED_LABEL, Boolean.FALSE.toString());
        var status = post.getStatus();

        var condition = new Condition();
        condition.setType("CancelledPublish");
        condition.setStatus(ConditionStatus.TRUE);
        condition.setReason(condition.getType());
        condition.setMessage("CancelledPublish");
        condition.setLastTransitionTime(Instant.now());
        status.getConditionsOrDefault().addAndEvictFIFO(condition);

        status.setPhase(PostPhase.DRAFT.toString());

        events.add(new PostUnpublishedEvent(this, post.getMetadata().getName()));
    }

    private void cleanUpResources(Post post) {
        // clean up snapshots
        final Ref ref = Ref.of(post);
        client.list(Snapshot.class,
                snapshot -> ref.equals(snapshot.getSpec().getSubjectRef()), null)
            .forEach(client::delete);

        // clean up comments
        client.list(Comment.class, comment -> ref.equals(comment.getSpec().getSubjectRef()),
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
