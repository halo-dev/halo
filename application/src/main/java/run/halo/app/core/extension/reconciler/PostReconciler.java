package run.halo.app.core.extension.reconciler;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.BooleanUtils.TRUE;
import static org.apache.commons.lang3.BooleanUtils.isFalse;
import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static run.halo.app.extension.ExtensionUtil.addFinalizers;
import static run.halo.app.extension.ExtensionUtil.removeFinalizers;
import static run.halo.app.extension.MetadataUtil.nullSafeAnnotations;
import static run.halo.app.extension.MetadataUtil.nullSafeLabels;
import static run.halo.app.extension.index.query.QueryFactory.equal;

import com.google.common.hash.Hashing;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import run.halo.app.content.ContentWrapper;
import run.halo.app.content.NotificationReasonConst;
import run.halo.app.content.PostService;
import run.halo.app.content.comment.CommentService;
import run.halo.app.content.permalinks.PostPermalinkPolicy;
import run.halo.app.core.extension.content.Constant;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.content.Post.PostPhase;
import run.halo.app.core.extension.content.Post.VisibleEnum;
import run.halo.app.core.extension.content.Snapshot;
import run.halo.app.core.extension.notification.Subscription;
import run.halo.app.event.post.PostDeletedEvent;
import run.halo.app.event.post.PostPublishedEvent;
import run.halo.app.event.post.PostUnpublishedEvent;
import run.halo.app.event.post.PostUpdatedEvent;
import run.halo.app.event.post.PostVisibleChangedEvent;
import run.halo.app.extension.DefaultExtensionMatcher;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ExtensionOperator;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.Ref;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.controller.RequeueException;
import run.halo.app.extension.index.query.QueryFactory;
import run.halo.app.extension.router.selector.FieldSelector;
import run.halo.app.infra.Condition;
import run.halo.app.infra.ConditionStatus;
import run.halo.app.infra.utils.HaloUtils;
import run.halo.app.metrics.CounterService;
import run.halo.app.metrics.MeterUtils;
import run.halo.app.notification.NotificationCenter;

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
    private final CommentService commentService;

    private final ApplicationEventPublisher eventPublisher;
    private final NotificationCenter notificationCenter;

    @Override
    public Result reconcile(Request request) {
        var events = new HashSet<ApplicationEvent>();
        client.fetch(Post.class, request.name())
            .ifPresent(post -> {
                if (ExtensionOperator.isDeleted(post)) {
                    removeFinalizers(post.getMetadata(), Set.of(FINALIZER_NAME));
                    unPublishPost(post, events);
                    events.add(new PostDeletedEvent(this, post));
                    cleanUpResources(post);
                    // update post to be able to be collected by gc collector.
                    client.update(post);
                    // fire event after updating post
                    events.forEach(eventPublisher::publishEvent);
                    return;
                }
                addFinalizers(post.getMetadata(), Set.of(FINALIZER_NAME));

                populateLabels(post);

                schedulePublishIfNecessary(post);

                subscribeNewCommentNotification(post);

                var status = post.getStatus();
                if (status == null) {
                    status = new Post.PostStatus();
                    post.setStatus(status);
                }

                if (post.isPublished() && post.getSpec().getPublishTime() == null) {
                    post.getSpec().setPublishTime(Instant.now());
                }

                // calculate the sha256sum
                var configSha256sum = Hashing.sha256().hashString(post.getSpec().toString(), UTF_8)
                    .toString();

                var annotations = nullSafeAnnotations(post);
                var oldConfigChecksum = annotations.get(Constant.CHECKSUM_CONFIG_ANNO);
                if (!Objects.equals(oldConfigChecksum, configSha256sum)) {
                    // if the checksum doesn't match
                    events.add(new PostUpdatedEvent(this, post.getMetadata().getName()));
                    annotations.put(Constant.CHECKSUM_CONFIG_ANNO, configSha256sum);
                }

                if (shouldUnPublish(post)) {
                    unPublishPost(post, events);
                } else {
                    publishPost(post, events);
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
                var contributors = listSnapshots(ref)
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

                // version + 1 is required to truly equal version
                // as a version will be incremented after the update
                status.setObservedVersion(post.getMetadata().getVersion() + 1);
                client.update(post);

                // fire event after updating post
                events.forEach(eventPublisher::publishEvent);
            });
        return Result.doNotRetry();
    }

    private void populateLabels(Post post) {
        var labels = nullSafeLabels(post);
        labels.put(Post.DELETED_LABEL, String.valueOf(isTrue(post.getSpec().getDeleted())));

        var expectVisible = defaultIfNull(post.getSpec().getVisible(), VisibleEnum.PUBLIC);
        var oldVisible = VisibleEnum.from(labels.get(Post.VISIBLE_LABEL));
        if (!Objects.equals(oldVisible, expectVisible)) {
            var postName = post.getMetadata().getName();
            eventPublisher.publishEvent(
                new PostVisibleChangedEvent(postName, oldVisible, expectVisible));
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

        if (!labels.containsKey(Post.PUBLISHED_LABEL)) {
            labels.put(Post.PUBLISHED_LABEL, BooleanUtils.FALSE);
        }
    }

    private static boolean shouldUnPublish(Post post) {
        return isTrue(post.getSpec().getDeleted()) || isFalse(post.getSpec().getPublish());
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return builder
            .extension(new Post())
            .onAddMatcher(DefaultExtensionMatcher.builder(client, Post.GVK)
                .fieldSelector(FieldSelector.of(
                    equal(Post.REQUIRE_SYNC_ON_STARTUP_INDEX_NAME, TRUE))
                )
                .build()
            )
            .build();
    }

    void schedulePublishIfNecessary(Post post) {
        var labels = nullSafeLabels(post);
        // ensure the label is removed
        labels.remove(Post.SCHEDULING_PUBLISH_LABEL);

        final var now = Instant.now();
        var publishTime = post.getSpec().getPublishTime();
        if (post.isPublished() || publishTime == null) {
            return;
        }

        // expect to publish in the future
        if (isTrue(post.getSpec().getPublish()) && publishTime.isAfter(now)) {
            labels.put(Post.SCHEDULING_PUBLISH_LABEL, TRUE);
            // update post changes before requeue
            client.update(post);

            throw new RequeueException(Result.requeue(Duration.between(now, publishTime)),
                "Requeue for scheduled publish.");
        }
    }

    void subscribeNewCommentNotification(Post post) {
        var subscriber = new Subscription.Subscriber();
        subscriber.setName(post.getSpec().getOwner());

        var interestReason = new Subscription.InterestReason();
        interestReason.setReasonType(NotificationReasonConst.NEW_COMMENT_ON_POST);
        interestReason.setExpression(
            "props.postOwner == '%s'".formatted(post.getSpec().getOwner()));
        notificationCenter.subscribe(subscriber, interestReason).block();
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
        final var status = post.getStatus();

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
        listSnapshots(ref).forEach(client::delete);

        // clean up comments
        commentService.removeBySubject(ref).block();

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

    List<Snapshot> listSnapshots(Ref ref) {
        var snapshotListOptions = new ListOptions();
        snapshotListOptions.setFieldSelector(FieldSelector.of(
            QueryFactory.equal("spec.subjectRef", Snapshot.toSubjectRefKey(ref))));
        return client.listAll(Snapshot.class, snapshotListOptions, Sort.unsorted());
    }
}
