package run.halo.app.core.extension.reconciler;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.web.util.UriUtils.encodePath;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.springframework.context.ApplicationContext;
import run.halo.app.content.ContentService;
import run.halo.app.content.SinglePageService;
import run.halo.app.content.permalinks.ExtensionLocator;
import run.halo.app.core.extension.Comment;
import run.halo.app.core.extension.Post;
import run.halo.app.core.extension.SinglePage;
import run.halo.app.core.extension.Snapshot;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ExtensionOperator;
import run.halo.app.extension.ExtensionUtil;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.Ref;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.infra.Condition;
import run.halo.app.infra.ConditionStatus;
import run.halo.app.infra.ExternalUrlSupplier;
import run.halo.app.infra.utils.JsonUtils;
import run.halo.app.metrics.CounterService;
import run.halo.app.metrics.MeterUtils;
import run.halo.app.theme.router.PermalinkIndexAddCommand;
import run.halo.app.theme.router.PermalinkIndexDeleteCommand;

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
public class SinglePageReconciler implements Reconciler<Reconciler.Request> {
    private static final String FINALIZER_NAME = "single-page-protection";
    private static final GroupVersionKind GVK = GroupVersionKind.fromExtension(SinglePage.class);
    private final ExtensionClient client;
    private final ContentService contentService;
    private final ApplicationContext applicationContext;
    private final SinglePageService singlePageService;
    private final CounterService counterService;

    private final ExternalUrlSupplier externalUrlSupplier;

    public SinglePageReconciler(ExtensionClient client, ContentService contentService,
        ApplicationContext applicationContext, SinglePageService singlePageService,
        CounterService counterService,
        ExternalUrlSupplier externalUrlSupplier) {
        this.client = client;
        this.contentService = contentService;
        this.applicationContext = applicationContext;
        this.singlePageService = singlePageService;
        this.counterService = counterService;
        this.externalUrlSupplier = externalUrlSupplier;
    }

    @Override
    public Result reconcile(Request request) {
        client.fetch(SinglePage.class, request.name())
            .ifPresent(singlePage -> {
                SinglePage oldPage = JsonUtils.deepCopy(singlePage);
                if (ExtensionOperator.isDeleted(singlePage)) {
                    cleanUpResourcesAndRemoveFinalizer(request.name());
                    return;
                }
                addFinalizerIfNecessary(oldPage);

                // reconcile spec first
                reconcileSpec(request.name());
                // then
                reconcileMetadata(request.name());
                reconcileStatus(request.name());
            });
        return new Result(false, null);
    }

    private void reconcileSpec(String name) {
        // publish single page if necessary
        singlePageService.publish(name).block();

        client.fetch(SinglePage.class, name).ifPresent(page -> {
            SinglePage oldPage = JsonUtils.deepCopy(page);
            if (page.isPublished() && Objects.equals(false, page.getSpec().getPublish())) {
                SinglePage.changePublishedState(page, false);
                final SinglePage.SinglePageStatus status = page.getStatusOrDefault();
                Condition condition = new Condition();
                condition.setType("CancelledPublish");
                condition.setStatus(ConditionStatus.TRUE);
                condition.setReason(condition.getType());
                condition.setMessage("CancelledPublish");
                condition.setLastTransitionTime(Instant.now());
                status.getConditionsOrDefault().add(condition);
                status.setPhase(Post.PostPhase.DRAFT.name());
            }
            if (!oldPage.equals(page)) {
                client.update(page);
            }
        });
    }

    private void addFinalizerIfNecessary(SinglePage oldSinglePage) {
        Set<String> finalizers = oldSinglePage.getMetadata().getFinalizers();
        if (finalizers != null && finalizers.contains(FINALIZER_NAME)) {
            return;
        }
        client.fetch(SinglePage.class, oldSinglePage.getMetadata().getName())
            .ifPresent(singlePage -> {
                Set<String> newFinalizers = singlePage.getMetadata().getFinalizers();
                if (newFinalizers == null) {
                    newFinalizers = new HashSet<>();
                    singlePage.getMetadata().setFinalizers(newFinalizers);
                }
                newFinalizers.add(FINALIZER_NAME);
                client.update(singlePage);
            });
    }

    private void cleanUpResources(SinglePage singlePage) {
        // remove permalink from permalink indexer
        permalinkOnDelete(singlePage);

        // clean up snapshot
        Ref ref = Ref.of(singlePage);
        client.list(Snapshot.class,
                snapshot -> ref.equals(snapshot.getSpec().getSubjectRef()), null)
            .forEach(client::delete);

        // clean up comments
        client.list(Comment.class, comment -> comment.getSpec().getSubjectRef().equals(ref),
                null)
            .forEach(client::delete);

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
            Map<String, String> labels = ExtensionUtil.nullSafeLabels(singlePage);
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

    private void permalinkOnDelete(SinglePage singlePage) {
        ExtensionLocator locator = new ExtensionLocator(GVK, singlePage.getMetadata().getName(),
            singlePage.getSpec().getSlug());
        applicationContext.publishEvent(new PermalinkIndexDeleteCommand(this, locator));
    }

    String createPermalink(SinglePage page) {
        var permalink = encodePath(page.getSpec().getSlug(), UTF_8);
        permalink = StringUtils.prependIfMissing(permalink, "/");
        return externalUrlSupplier.get().resolve(permalink).normalize().toString();
    }

    private void permalinkOnAdd(SinglePage singlePage) {
        if (!singlePage.isPublished() || Objects.equals(true, singlePage.getSpec().getDeleted())) {
            return;
        }
        ExtensionLocator locator = new ExtensionLocator(GVK, singlePage.getMetadata().getName(),
            singlePage.getSpec().getSlug());
        applicationContext.publishEvent(new PermalinkIndexAddCommand(this, locator,
            singlePage.getStatusOrDefault().getPermalink()));
    }

    private void reconcileStatus(String name) {
        client.fetch(SinglePage.class, name).ifPresent(singlePage -> {
            final SinglePage oldPage = JsonUtils.deepCopy(singlePage);
            permalinkOnDelete(oldPage);

            singlePage.getStatusOrDefault()
                .setPermalink(createPermalink(singlePage));
            permalinkOnAdd(singlePage);

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
                contentService.getContent(spec.getHeadSnapshot())
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
            contentService.listSnapshots(Ref.of(singlePage))
                .collectList()
                .blockOptional().ifPresent(snapshots -> {
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
                            status.setInProgress(!snapshot.isPublished());
                        });

                    List<String> releasedSnapshots = snapshots.stream()
                        .filter(Snapshot::isPublished)
                        .sorted(Comparator.comparing(snapshot -> snapshot.getSpec().getVersion()))
                        .map(snapshot -> snapshot.getMetadata().getName())
                        .toList();
                    status.setReleasedSnapshots(releasedSnapshots);
                });

            status.setConditions(limitConditionSize(status.getConditions()));

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

    static List<Condition> limitConditionSize(List<Condition> conditions) {
        if (conditions == null || conditions.size() <= 10) {
            return conditions;
        }
        // Retain the last ten conditions
        return conditions.subList(conditions.size() - 10, conditions.size());
    }
}
