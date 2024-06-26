package run.halo.app.content;

import static run.halo.app.extension.index.query.QueryFactory.equal;

import org.springframework.context.event.EventListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.content.Post;
import run.halo.app.event.post.CategoryHiddenStateChangeEvent;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.router.selector.FieldSelector;
import run.halo.app.infra.ReactiveExtensionPaginatedOperator;

/**
 * Synchronize the {@link Post.PostStatus#getHideFromList()} state of the post with the category.
 *
 * @author guqing
 * @since 2.17.0
 */
@Component
public class PostHideFromListStateUpdater
    extends AbstractEventReconciler<CategoryHiddenStateChangeEvent> {
    private final ReactiveExtensionPaginatedOperator reactiveExtensionPaginatedOperator;
    private final ReactiveExtensionClient client;

    protected PostHideFromListStateUpdater(ReactiveExtensionClient client,
        ReactiveExtensionPaginatedOperator reactiveExtensionPaginatedOperator) {
        super(PostHideFromListStateUpdater.class.getName());
        this.reactiveExtensionPaginatedOperator = reactiveExtensionPaginatedOperator;
        this.client = client;
    }

    @Override
    public Result reconcile(CategoryHiddenStateChangeEvent request) {
        var listOptions = new ListOptions();
        listOptions.setFieldSelector(FieldSelector.of(
            equal("spec.categories", request.getCategoryName())
        ));

        reactiveExtensionPaginatedOperator.list(Post.class, listOptions)
            .flatMap(post -> {
                post.getStatusOrDefault().setHideFromList(request.isHidden());
                return client.update(post);
            })
            .then()
            .block();
        return Result.doNotRetry();
    }

    @EventListener(CategoryHiddenStateChangeEvent.class)
    public void onApplicationEvent(@NonNull CategoryHiddenStateChangeEvent event) {
        this.queue.addImmediately(event);
    }
}
