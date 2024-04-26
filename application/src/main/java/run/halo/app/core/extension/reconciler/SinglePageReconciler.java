package run.halo.app.core.extension.reconciler;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.web.util.UriUtils.encodePath;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import run.halo.app.content.NotificationReasonConst;
import run.halo.app.content.SinglePageService;
import run.halo.app.content.comment.CommentService;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.core.extension.content.Snapshot;
import run.halo.app.core.extension.notification.Subscription;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ExtensionOperator;
import run.halo.app.extension.ExtensionUtil;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.Ref;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.index.query.QueryFactory;
import run.halo.app.extension.router.selector.FieldSelector;
import run.halo.app.infra.Condition;
import run.halo.app.infra.ConditionList;
import run.halo.app.infra.ConditionStatus;
import run.halo.app.infra.ExternalUrlSupplier;
import run.halo.app.infra.utils.JsonUtils;
import run.halo.app.metrics.CounterService;
import run.halo.app.metrics.MeterUtils;
import run.halo.app.notification.NotificationCenter;

/**
 * <p>Reconciler for {@link SinglePage}.</p>
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
@Slf4j
@AllArgsConstructor
@Component
public class SinglePageReconciler implements Reconciler<Reconciler.Request> {
    private static final String FINALIZER_NAME = "single-page-protection";
    private final ExtensionClient client;
    private final SinglePageService singlePageService;
    private final CounterService counterService;
    private final CommentService commentService;

    private final ExternalUrlSupplier externalUrlSupplier;

    private final NotificationCenter notificationCenter;

    @Override
    public Result reconcile(Request request) {
        client.fetch(SinglePage.class, request.name())
            .ifPresent(singlePage -> {
                if (ExtensionOperator.isDeleted(singlePage)) {
                    cleanUpResourcesAndRemoveFinalizer(request.name());
                    return;
                }

                if (ExtensionUtil.addFinalizers(singlePage.getMetadata(), Set.of(FINALIZER_NAME))) {
                    client.update(singlePage);
                }

                subscribeNewCommentNotification(singlePage);

                // reconcile spec first
                reconcileSpec(request.name());
                // then
                reconcileMetadata(request.name());
                reconcileStatus(request.name());
            });
        return new Result(false, null);
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return builder
            .extension(new SinglePage())
            .build();
    }

    void subscribeNewCommentNotification(SinglePage page) {
        var subscriber = new Subscription.Subscriber();
        subscriber.setName(page.getSpec().getOwner());

        var interestReason = new Subscription.InterestReason();
        interestReason.setReasonType(NotificationReasonConst.NEW_COMMENT_ON_PAGE);
        interestReason.setExpression(
            "props.pageOwner == '%s'".formatted(page.getSpec().getOwner()));
        notificationCenter.subscribe(subscriber, interestReason).block();
    }

    private void reconcileSpec(String name) {
        client.fetch(SinglePage.class, name).ifPresent(page -> {
            if (page.isPublished() && page.getSpec().getPublishTime() == null) {
                page.getSpec().setPublishTime(Instant.now());
            }

            // un-publish if necessary
            if (page.isPublished() && Objects.equals(false, page.getSpec().getPublish())) {
                unPublish(name);
                return;
            }

            try {
                publishPage(name);
            } catch (Throwable e) {
                publishFailed(name, e);
                throw e;
            }
        });
    }

    private void publishPage(String name) {
        client.fetch(SinglePage.class, name)
            .filter(page -> Objects.equals(true, page.getSpec().getPublish()))
            .ifPresent(page -> {
                Map<String, String> annotations = MetadataUtil.nullSafeAnnotations(page);
                String lastReleasedSnapshot = annotations.get(Post.LAST_RELEASED_SNAPSHOT_ANNO);
                String releaseSnapshot = page.getSpec().getReleaseSnapshot();
                if (StringUtils.isBlank(releaseSnapshot)) {
                    return;
                }
                // do nothing if release snapshot is not changed and page is published
                if (page.isPublished()
                    && StringUtils.equals(lastReleasedSnapshot, releaseSnapshot)) {
                    return;
                }
                SinglePage.SinglePageStatus status = page.getStatusOrDefault();

                // validate release snapshot
                Optional<Snapshot> releasedSnapshotOpt =
                    client.fetch(Snapshot.class, releaseSnapshot);
                if (releasedSnapshotOpt.isEmpty()) {
                    Condition condition = Condition.builder()
                        .type(Post.PostPhase.FAILED.name())
                        .reason("SnapshotNotFound")
                        .message(
                            String.format("Snapshot [%s] not found for publish", releaseSnapshot))
                        .status(ConditionStatus.FALSE)
                        .lastTransitionTime(Instant.now())
                        .build();
                    status.getConditionsOrDefault().addAndEvictFIFO(condition);
                    status.setPhase(Post.PostPhase.FAILED.name());
                    client.update(page);
                    return;
                }

                // do publish
                annotations.put(SinglePage.LAST_RELEASED_SNAPSHOT_ANNO, releaseSnapshot);
                status.setPhase(Post.PostPhase.PUBLISHED.name());
                Condition condition = Condition.builder()
                    .type(Post.PostPhase.PUBLISHED.name())
                    .reason("Published")
                    .message("SinglePage published successfully.")
                    .lastTransitionTime(Instant.now())
                    .status(ConditionStatus.TRUE)
                    .build();
                status.getConditionsOrDefault().addAndEvictFIFO(condition);

                SinglePage.changePublishedState(page, true);
                if (page.getSpec().getPublishTime() == null) {
                    page.getSpec().setPublishTime(Instant.now());
                }

                // populate lastModifyTime
                status.setLastModifyTime(releasedSnapshotOpt.get().getSpec().getLastModifyTime());

                client.update(page);
            });
    }

    private void unPublish(String name) {
        client.fetch(SinglePage.class, name).ifPresent(page -> {
            final SinglePage oldPage = JsonUtils.deepCopy(page);

            SinglePage.changePublishedState(page, false);
            final SinglePage.SinglePageStatus status = page.getStatusOrDefault();

            Condition condition = new Condition();
            condition.setType("CancelledPublish");
            condition.setStatus(ConditionStatus.TRUE);
            condition.setReason(condition.getType());
            condition.setMessage("CancelledPublish");
            condition.setLastTransitionTime(Instant.now());
            status.getConditionsOrDefault().addAndEvictFIFO(condition);

            status.setPhase(Post.PostPhase.DRAFT.name());
            if (!oldPage.equals(page)) {
                client.update(page);
            }
        });
    }

    private void publishFailed(String name, Throwable error) {
        Assert.notNull(name, "Name must not be null");
        Assert.notNull(error, "Error must not be null");
        client.fetch(SinglePage.class, name).ifPresent(page -> {
            final SinglePage oldPage = JsonUtils.deepCopy(page);

            SinglePage.SinglePageStatus status = page.getStatusOrDefault();
            Post.PostPhase phase = Post.PostPhase.FAILED;
            status.setPhase(phase.name());

            final ConditionList conditions = status.getConditionsOrDefault();

            Condition condition = Condition.builder()
                .type(phase.name())
                .reason("PublishFailed")
                .message(error.getMessage())
                .lastTransitionTime(Instant.now())
                .status(ConditionStatus.FALSE)
                .build();
            conditions.addAndEvictFIFO(condition);
            page.setStatus(status);

            if (!oldPage.equals(page)) {
                client.update(page);
            }
        });
    }

    private void cleanUpResources(SinglePage singlePage) {
        // clean up snapshot
        Ref ref = Ref.of(singlePage);
        listSnapshots(ref).forEach(client::delete);

        // clean up comments
        commentService.removeBySubject(ref).block();

        // delete counter for single page
        counterService.deleteByName(
                MeterUtils.nameOf(SinglePage.class, singlePage.getMetadata().getName()))
            .block();
    }

    private void cleanUpResourcesAndRemoveFinalizer(String pageName) {
        client.fetch(SinglePage.class, pageName).ifPresent(singlePage -> {
            cleanUpResources(singlePage);
            if (singlePage.getMetadata().getFinalizers() != null) {
                singlePage.getMetadata().getFinalizers().remove(FINALIZER_NAME);
            }
            client.update(singlePage);
        });
    }

    private void reconcileMetadata(String name) {
        client.fetch(SinglePage.class, name).ifPresent(singlePage -> {
            final SinglePage oldPage = JsonUtils.deepCopy(singlePage);

            SinglePage.SinglePageSpec spec = singlePage.getSpec();
            // handle logic delete
            Map<String, String> labels = MetadataUtil.nullSafeLabels(singlePage);
            if (isDeleted(singlePage)) {
                labels.put(SinglePage.DELETED_LABEL, Boolean.TRUE.toString());
            } else {
                labels.put(SinglePage.DELETED_LABEL, Boolean.FALSE.toString());
            }
            labels.put(SinglePage.VISIBLE_LABEL,
                Objects.requireNonNullElse(spec.getVisible(), Post.VisibleEnum.PUBLIC).name());
            labels.put(SinglePage.OWNER_LABEL, spec.getOwner());
            if (!labels.containsKey(SinglePage.PUBLISHED_LABEL)) {
                labels.put(Post.PUBLISHED_LABEL, Boolean.FALSE.toString());
            }
            if (!oldPage.equals(singlePage)) {
                client.update(singlePage);
            }
        });
    }

    String createPermalink(SinglePage page) {
        var permalink = encodePath(page.getSpec().getSlug(), UTF_8);
        permalink = StringUtils.prependIfMissing(permalink, "/");
        return externalUrlSupplier.get().resolve(permalink).normalize().toString();
    }

    private void reconcileStatus(String name) {
        client.fetch(SinglePage.class, name).ifPresent(singlePage -> {
            final SinglePage oldPage = JsonUtils.deepCopy(singlePage);

            singlePage.getStatusOrDefault()
                .setPermalink(createPermalink(singlePage));

            SinglePage.SinglePageSpec spec = singlePage.getSpec();
            SinglePage.SinglePageStatus status = singlePage.getStatusOrDefault();
            if (status.getPhase() == null) {
                status.setPhase(Post.PostPhase.DRAFT.name());
            }

            // handle excerpt
            Post.Excerpt excerpt = spec.getExcerpt();
            if (excerpt == null) {
                excerpt = new Post.Excerpt();
                excerpt.setAutoGenerate(true);
                spec.setExcerpt(excerpt);
            }

            if (excerpt.getAutoGenerate()) {
                singlePageService.getContent(spec.getHeadSnapshot(), spec.getBaseSnapshot())
                    .blockOptional()
                    .ifPresent(content -> {
                        String contentRevised = content.getContent();
                        status.setExcerpt(getExcerpt(contentRevised));
                    });
            } else {
                status.setExcerpt(excerpt.getRaw());
            }

            // handle contributors
            String headSnapshot = singlePage.getSpec().getHeadSnapshot();
            List<String> contributors = listSnapshots(Ref.of(singlePage))
                .stream()
                .peek(snapshot -> {
                    snapshot.getSpec().setContentPatch(StringUtils.EMPTY);
                    snapshot.getSpec().setRawPatch(StringUtils.EMPTY);
                })
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
            String releaseSnapshot = singlePage.getSpec().getReleaseSnapshot();
            status.setInProgress(!StringUtils.equals(releaseSnapshot, headSnapshot));

            if (singlePage.isPublished() && status.getLastModifyTime() == null) {
                client.fetch(Snapshot.class, singlePage.getSpec().getReleaseSnapshot())
                    .ifPresent(releasedSnapshot ->
                        status.setLastModifyTime(releasedSnapshot.getSpec().getLastModifyTime()));
            }

            if (!oldPage.equals(singlePage)) {
                client.update(singlePage);
            }
        });
    }

    private String getExcerpt(String htmlContent) {
        String shortHtmlContent = StringUtils.substring(htmlContent, 0, 500);
        String text = Jsoup.parse(shortHtmlContent).text();
        // TODO The default capture 150 words as excerpt
        return StringUtils.substring(text, 0, 150);
    }

    private boolean isDeleted(SinglePage singlePage) {
        return Objects.equals(true, singlePage.getSpec().getDeleted())
            || singlePage.getMetadata().getDeletionTimestamp() != null;
    }

    List<Snapshot> listSnapshots(Ref ref) {
        var snapshotListOptions = new ListOptions();
        snapshotListOptions.setFieldSelector(FieldSelector.of(
            QueryFactory.equal("spec.subjectRef", Snapshot.toSubjectRefKey(ref))));
        return client.listAll(Snapshot.class, snapshotListOptions, Sort.unsorted());
    }
}
