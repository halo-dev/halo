package run.halo.app.core.extension.reconciler;

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
                reconcilePermalink(category);

                if (!oldCategory.equals(category)) {
                    client.update(category);
                }
            });
        return new Result(false, null);
    }

    private void reconcilePermalink(Category category) {
        if (category.getStatusOrDefault().getPermalink() == null) {
            category.getStatusOrDefault()
                .setPermalink(categoryPermalinkPolicy.permalink(category));
        }

        if (category.getMetadata().getDeletionTimestamp() != null) {
            categoryPermalinkPolicy.onPermalinkDelete(category);
            return;
        }
        categoryPermalinkPolicy.onPermalinkAdd(category);
    }
}
