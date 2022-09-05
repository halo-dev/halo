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
import run.halo.app.theme.router.PermalinkIndexDeleteCommand;
import run.halo.app.theme.router.PermalinkIndexUpdateCommand;
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

                doReconcile(singlePage);
                permalinkReconcile(singlePage);

                if (!oldPage.equals(singlePage)) {
                    client.update(singlePage);
                }
            });
        return new Result(false, null);
    }

    private void permalinkReconcile(SinglePage singlePage) {
        singlePage.getStatusOrDefault()
            .setPermalink(PathUtils.combinePath(singlePage.getSpec().getSlug()));

        GroupVersionKind gvk = GroupVersionKind.fromExtension(SinglePage.class);
        ExtensionLocator locator = new ExtensionLocator(gvk, singlePage.getMetadata().getName(),
            singlePage.getSpec().getSlug());
        if (Objects.equals(true, singlePage.getSpec().getDeleted())
            || singlePage.getMetadata().getDeletionTimestamp() != null
            || Objects.equals(false, singlePage.getSpec().getPublished())) {
            applicationContext.publishEvent(new PermalinkIndexDeleteCommand(this, locator,
                singlePage.getStatusOrDefault().getPermalink()));
        } else {
            applicationContext.publishEvent(new PermalinkIndexUpdateCommand(this, locator,
                singlePage.getStatusOrDefault().getPermalink()));
        }

        templateRouteManager.changeTemplatePattern(DefaultTemplateEnum.SINGLE_PAGE.getValue());
    }

    private void doReconcile(SinglePage singlePage) {
        String name = singlePage.getMetadata().getName();
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
                .subscribe(content -> {
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
        Map<String, String> labels = getLabelsOrDefault(singlePage);
        if (isDeleted(singlePage)) {
            labels.put(SinglePage.DELETED_LABEL, Boolean.TRUE.toString());
        } else {
            labels.put(SinglePage.DELETED_LABEL, Boolean.FALSE.toString());
        }
        // synchronize some fields to labels to query
        labels.put(SinglePage.PHASE_LABEL, status.getPhase());
        labels.put(SinglePage.VISIBLE_LABEL,
            Objects.requireNonNullElse(spec.getVisible(), Post.VisibleEnum.PUBLIC).name());
        labels.put(SinglePage.OWNER_LABEL, spec.getOwner());
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

    private boolean isDeleted(SinglePage singlePage) {
        return Objects.equals(true, singlePage.getSpec().getDeleted())
            || singlePage.getMetadata().getDeletionTimestamp() != null;
    }
}
