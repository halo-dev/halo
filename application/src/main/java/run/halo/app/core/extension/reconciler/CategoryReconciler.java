package run.halo.app.core.extension.reconciler;

import static run.halo.app.extension.ExtensionUtil.addFinalizers;
import static run.halo.app.extension.ExtensionUtil.removeFinalizers;

import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import run.halo.app.content.permalinks.CategoryPermalinkPolicy;
import run.halo.app.core.extension.content.Category;
import run.halo.app.core.extension.content.Constant;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ExtensionUtil;
import run.halo.app.extension.MetadataUtil;
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

    @Override
    public Result reconcile(Request request) {
        client.fetch(Category.class, request.name())
            .ifPresent(category -> {
                if (ExtensionUtil.isDeleted(category)) {
                    if (removeFinalizers(category.getMetadata(), Set.of(FINALIZER_NAME))) {
                        client.update(category);
                    }
                    return;
                }
                addFinalizers(category.getMetadata(), Set.of(FINALIZER_NAME));

                populatePermalinkPattern(category);
                populatePermalink(category);

                client.update(category);
            });
        return Result.doNotRetry();
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return builder
            .extension(new Category())
            .build();
    }

    void populatePermalinkPattern(Category category) {
        Map<String, String> annotations = MetadataUtil.nullSafeAnnotations(category);
        String newPattern = categoryPermalinkPolicy.pattern();
        annotations.put(Constant.PERMALINK_PATTERN_ANNO, newPattern);
    }

    void populatePermalink(Category category) {
        category.getStatusOrDefault()
            .setPermalink(categoryPermalinkPolicy.permalink(category));
    }
}
