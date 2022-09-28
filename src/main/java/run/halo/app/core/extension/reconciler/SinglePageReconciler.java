package run.halo.app.core.extension.reconciler;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.web.util.UriUtils.encodePath;

import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;
import run.halo.app.content.ContentService;
import run.halo.app.content.permalinks.ExtensionLocator;
import run.halo.app.core.extension.Post;
import run.halo.app.core.extension.SinglePage;
import run.halo.app.core.extension.Snapshot;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.infra.Condition;
import run.halo.app.infra.ConditionStatus;
import run.halo.app.infra.utils.JsonUtils;
import run.halo.app.infra.utils.PathUtils;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.router.PermalinkIndexAddCommand;
import run.halo.app.theme.router.PermalinkIndexDeleteCommand;
import run.halo.app.theme.router.TemplateRouteManager;

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
    private final TemplateRouteManager templateRouteManager;

    public SinglePageReconciler(ExtensionClient client, ContentService contentService,
        ApplicationContext applicationContext, TemplateRouteManager templateRouteManager) {
        this.client = client;
        this.contentService = contentService;
        this.applicationContext = applicationContext;
        this.templateRouteManager = templateRouteManager;
    }

    @Override
    public Result reconcile(Request request) {
        client.fetch(SinglePage.class, request.name())
            .ifPresent(singlePage -> {
                SinglePage oldPage = JsonUtils.deepCopy(singlePage);
                if (isDeleted(oldPage)) {
                    cleanUpResourcesAndRemoveFinalizer(request.name());
                    return;
                }
                addFinalizerIfNecessary(oldPage);

                reconcileStatus(request.name());
                reconcileMetadata(request.name());
            });
        return new Result(false, null);
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
            Map<String, String> labels = getLabelsOrDefault(singlePage);
            if (isDeleted(singlePage)) {
                labels.put(SinglePage.DELETED_LABEL, Boolean.TRUE.toString());
            } else {
                labels.put(SinglePage.DELETED_LABEL, Boolean.FALSE.toString());
            }
            // synchronize some fields to labels to query
            labels.put(SinglePage.PHASE_LABEL, singlePage.getStatusOrDefault().getPhase());
            labels.put(SinglePage.VISIBLE_LABEL,
                Objects.requireNonNullElse(spec.getVisible(), Post.VisibleEnum.PUBLIC).name());
            labels.put(SinglePage.OWNER_LABEL, spec.getOwner());
            if (!oldPage.equals(singlePage)) {
                client.update(singlePage);
            }
        });
    }

    private void permalinkOnDelete(SinglePage singlePage) {
        singlePage.getStatusOrDefault()
            .setPermalink(PathUtils.combinePath(singlePage.getSpec().getSlug()));
        ExtensionLocator locator = new ExtensionLocator(GVK, singlePage.getMetadata().getName(),
            singlePage.getSpec().getSlug());
        applicationContext.publishEvent(new PermalinkIndexDeleteCommand(this, locator));
        templateRouteManager.changeTemplatePattern(DefaultTemplateEnum.SINGLE_PAGE.getValue());
    }

    private void permalinkOnAdd(SinglePage singlePage) {
        ExtensionLocator locator = new ExtensionLocator(GVK, singlePage.getMetadata().getName(),
            singlePage.getSpec().getSlug());
        applicationContext.publishEvent(new PermalinkIndexAddCommand(this, locator,
            singlePage.getStatusOrDefault().getPermalink()));
        templateRouteManager.changeTemplatePattern(DefaultTemplateEnum.SINGLE_PAGE.getValue());
    }

    private void reconcileStatus(String name) {
        client.fetch(SinglePage.class, name).ifPresent(singlePage -> {
            final SinglePage oldPage = JsonUtils.deepCopy(singlePage);
            permalinkOnDelete(oldPage);

            var permalink = encodePath(singlePage.getSpec().getSlug(), UTF_8);
            permalink = StringUtils.prependIfMissing(permalink, "/");
            singlePage.getStatusOrDefault()
                .setPermalink(permalink);
            if (isPublished(singlePage)) {
                permalinkOnAdd(singlePage);
            }

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
                        String contentRevised = content.content();
                        status.setExcerpt(getExcerpt(contentRevised));
                    });
            } else {
                status.setExcerpt(excerpt.getRaw());
            }

            // handle contributors
            String headSnapshot = singlePage.getSpec().getHeadSnapshot();
            contentService.listSnapshots(Snapshot.SubjectRef.of(SinglePage.KIND, name))
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

            if (!oldPage.equals(singlePage)) {
                client.update(singlePage);
            }
        });
    }

    private Map<String, String> getLabelsOrDefault(SinglePage singlePage) {
        Assert.notNull(singlePage, "The singlePage must not be null.");
        Map<String, String> labels = singlePage.getMetadata().getLabels();
        if (labels == null) {
            labels = new LinkedHashMap<>();
            singlePage.getMetadata().setLabels(labels);
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

    private boolean isPublished(SinglePage singlePage) {
        return Objects.equals(true, singlePage.getSpec().getPublished());
    }

    private boolean isDeleted(SinglePage singlePage) {
        return Objects.equals(true, singlePage.getSpec().getDeleted())
            || singlePage.getMetadata().getDeletionTimestamp() != null;
    }
}
