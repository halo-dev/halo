package run.halo.app.core.extension.reconciler;

import java.util.HashSet;
import java.util.Set;
import run.halo.app.content.permalinks.CategoryPermalinkPolicy;
import run.halo.app.core.extension.Category;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.infra.utils.JsonUtils;

/**
 * Reconciler for {@link Category}.
 *
 * @author guqing
 * @since 2.0.0
 */
public class CategoryReconciler implements Reconciler<Reconciler.Request> {
    private static final String FINALIZER_NAME = "category-protection";
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
                if (isDeleted(category)) {
                    cleanUpResourcesAndRemoveFinalizer(request.name());
                    return;
                }
                addFinalizerIfNecessary(category);

                reconcileStatus(request.name());
            });
        return new Result(false, null);
    }

    private void addFinalizerIfNecessary(Category oldCategory) {
        Set<String> finalizers = oldCategory.getMetadata().getFinalizers();
        if (finalizers != null && finalizers.contains(FINALIZER_NAME)) {
            return;
        }
        client.fetch(Category.class, oldCategory.getMetadata().getName())
            .ifPresent(category -> {
                Set<String> newFinalizers = category.getMetadata().getFinalizers();
                if (newFinalizers == null) {
                    newFinalizers = new HashSet<>();
                    category.getMetadata().setFinalizers(newFinalizers);
                }
                newFinalizers.add(FINALIZER_NAME);
                client.update(category);
            });
    }

    private void cleanUpResources(Category category) {
        // remove permalink from permalink indexer
        categoryPermalinkPolicy.onPermalinkDelete(category);
    }

    private void cleanUpResourcesAndRemoveFinalizer(String categoryName) {
        client.fetch(Category.class, categoryName).ifPresent(category -> {
            cleanUpResources(category);
            if (category.getMetadata().getFinalizers() != null) {
                category.getMetadata().getFinalizers().remove(FINALIZER_NAME);
            }
            client.update(category);
        });
    }

    private void reconcileStatus(String name) {
        client.fetch(Category.class, name)
            .ifPresent(category -> {
                Category oldCategory = JsonUtils.deepCopy(category);
                categoryPermalinkPolicy.onPermalinkDelete(oldCategory);

                category.getStatusOrDefault()
                    .setPermalink(categoryPermalinkPolicy.permalink(category));
                categoryPermalinkPolicy.onPermalinkAdd(category);

                if (!oldCategory.equals(category)) {
                    client.update(category);
                }
            });
    }

    private boolean isDeleted(Category category) {
        return category.getMetadata().getDeletionTimestamp() != null;
    }
}
