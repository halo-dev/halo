package run.halo.app.core.extension.reconciler;

import java.util.LinkedHashSet;
import java.util.Set;
import run.halo.app.content.permalinks.CategoryPermalinkPolicy;
import run.halo.app.core.extension.Category;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.infra.utils.JsonUtils;

/**
 * @author guqing
 * @since 2.0.0
 */
public class CategoryReconciler implements Reconciler<Reconciler.Request> {
    private static final String PERMALINK_FINALIZE = "permalink";
    private final ExtensionClient client;
    private final CategoryPermalinkPolicy categoryPermalinkPolicy;

    public CategoryReconciler(ExtensionClient client,
        CategoryPermalinkPolicy categoryPermalinkPolicy) {
        this.client = client;
        this.categoryPermalinkPolicy = categoryPermalinkPolicy;
    }

    @Override
    public Result reconcile(Request request) {
        client.fetch(Category.class, request.name())
            .ifPresent(category -> {
                Category oldCategory = JsonUtils.deepCopy(category);

                finalizeFlag(category);

                reconcilePermalink(category);

                if (!oldCategory.equals(category)) {
                    client.update(category);
                }
            });
        return new Result(false, null);
    }

    private void finalizeFlag(Category category) {
        Set<String> finalizers = category.getMetadata().getFinalizers();
        if (finalizers == null) {
            finalizers = new LinkedHashSet<>();
            category.getMetadata().setFinalizers(finalizers);
        }
        finalizers.add(PERMALINK_FINALIZE);
    }

    private void reconcilePermalink(Category category) {
        categoryPermalinkPolicy.onPermalinkDelete(category);

        category.getStatusOrDefault()
            .setPermalink(categoryPermalinkPolicy.permalink(category));

        if (!isDeleted(category)) {
            categoryPermalinkPolicy.onPermalinkAdd(category);
        }

        if (isDeleted(category) && category.getMetadata().getFinalizers() != null) {
            category.getMetadata().getFinalizers().remove(PERMALINK_FINALIZE);
        }
    }

    private boolean isDeleted(Category category) {
        return category.getMetadata().getDeletionTimestamp() != null;
    }
}
