package run.halo.app.core.reconciler;

import static run.halo.app.extension.ExtensionUtil.addFinalizers;
import static run.halo.app.extension.ExtensionUtil.removeFinalizers;
import static run.halo.app.extension.MetadataUtil.nullSafeAnnotations;

import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import run.halo.app.content.CategoryService;
import run.halo.app.content.permalinks.CategoryPermalinkPolicy;
import run.halo.app.core.extension.content.Category;
import run.halo.app.core.extension.content.Constant;
import run.halo.app.event.post.CategoryHiddenStateChangeEvent;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ExtensionUtil;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;

/**
 * Reconciler for {@link Category}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
@AllArgsConstructor
public class CategoryReconciler implements Reconciler<Reconciler.Request> {
    static final String FINALIZER_NAME = "category-protection";
    private final ExtensionClient client;
    private final CategoryPermalinkPolicy categoryPermalinkPolicy;
    private final CategoryService categoryService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Result reconcile(Request request) {
        client.fetch(Category.class, request.name())
            .ifPresent(category -> {
                if (ExtensionUtil.isDeleted(category)) {
                    if (removeFinalizers(category.getMetadata(), Set.of(FINALIZER_NAME))) {
                        refreshHiddenState(category, false);
                        client.update(category);
                    }
                    return;
                }
                addFinalizers(category.getMetadata(), Set.of(FINALIZER_NAME));

                populatePermalinkPattern(category);
                populatePermalink(category);
                checkHiddenState(category);

                client.update(category);
            });
        return Result.doNotRetry();
    }

    private void checkHiddenState(Category category) {
        final boolean hidden = categoryService.isCategoryHidden(category.getMetadata().getName())
            .blockOptional()
            .orElse(false);
        refreshHiddenState(category, hidden);
    }

    /**
     * TODO move this logic to before-create/update hook in the future see {@code gh-4343}.
     */
    private void refreshHiddenState(Category category, boolean hidden) {
        category.getSpec().setHideFromList(hidden);
        if (isHiddenStateChanged(category)) {
            publishHiddenStateChangeEvent(category);
        }
        var children = category.getSpec().getChildren();
        if (CollectionUtils.isEmpty(children)) {
            return;
        }
        for (String childName : children) {
            client.fetch(Category.class, childName)
                .ifPresent(child -> {
                    child.getSpec().setHideFromList(hidden);
                    if (isHiddenStateChanged(child)) {
                        publishHiddenStateChangeEvent(child);
                    }
                    client.update(child);
                });
        }
    }

    private void publishHiddenStateChangeEvent(Category category) {
        var hidden = category.getSpec().isHideFromList();
        nullSafeAnnotations(category).put(Category.LAST_HIDDEN_STATE_ANNO, String.valueOf(hidden));
        eventPublisher.publishEvent(new CategoryHiddenStateChangeEvent(this,
            category.getMetadata().getName(), hidden));
    }

    boolean isHiddenStateChanged(Category category) {
        var lastHiddenState = nullSafeAnnotations(category).get(Category.LAST_HIDDEN_STATE_ANNO);
        return !String.valueOf(category.getSpec().isHideFromList()).equals(lastHiddenState);
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return builder
            .extension(new Category())
            .build();
    }

    void populatePermalinkPattern(Category category) {
        Map<String, String> annotations = nullSafeAnnotations(category);
        String newPattern = categoryPermalinkPolicy.pattern();
        annotations.put(Constant.PERMALINK_PATTERN_ANNO, newPattern);
    }

    void populatePermalink(Category category) {
        category.getStatusOrDefault()
            .setPermalink(categoryPermalinkPolicy.permalink(category));
    }
}
