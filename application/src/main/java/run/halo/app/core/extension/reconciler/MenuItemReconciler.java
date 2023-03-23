package run.halo.app.core.extension.reconciler;

import java.time.Duration;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import run.halo.app.core.extension.MenuItem;
import run.halo.app.core.extension.MenuItem.MenuItemSpec;
import run.halo.app.core.extension.MenuItem.MenuItemStatus;
import run.halo.app.core.extension.content.Category;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.core.extension.content.Tag;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.Ref;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.controller.Reconciler.Request;

@Slf4j
@Component
public class MenuItemReconciler implements Reconciler<Request> {

    private final ExtensionClient client;

    public MenuItemReconciler(ExtensionClient client) {
        this.client = client;
    }

    @Override
    public Result reconcile(Request request) {
        return client.fetch(MenuItem.class, request.name())
            .map(menuItem -> {
                final var spec = menuItem.getSpec();

                if (menuItem.getStatus() == null) {
                    menuItem.setStatus(new MenuItemStatus());
                }
                var status = menuItem.getStatus();
                var targetRef = spec.getTargetRef();
                if (targetRef != null) {
                    if (Ref.groupKindEquals(targetRef, Category.GVK)) {
                        return handleCategoryRef(request.name(), status, targetRef);
                    }
                    if (Ref.groupKindEquals(targetRef, Tag.GVK)) {
                        return handleTagRef(request.name(), status, targetRef);
                    }
                    if (Ref.groupKindEquals(targetRef, SinglePage.GVK)) {
                        return handleSinglePageSpec(request.name(), status, targetRef);
                    }
                    if (Ref.groupKindEquals(targetRef, Post.GVK)) {
                        return handlePostRef(request.name(), status, targetRef);
                    }
                    // unsupported ref
                    log.error("Unsupported MenuItem targetRef " + targetRef);
                    return Result.doNotRetry();
                } else {
                    return handleMenuSpec(request.name(), status, spec);
                }
            }).orElseGet(() -> new Result(false, null));
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return builder
            .extension(new MenuItem())
            .build();
    }

    private Result handleCategoryRef(String menuItemName, MenuItemStatus status, Ref categoryRef) {
        client.fetch(Category.class, categoryRef.getName())
            .filter(category -> category.getStatus() != null)
            .filter(category -> StringUtils.hasText(category.getStatus().getPermalink()))
            .ifPresent(category -> {
                status.setHref(category.getStatus().getPermalink());
                status.setDisplayName(category.getSpec().getDisplayName());
                updateStatus(menuItemName, status);
            });
        return new Result(true, Duration.ofMinutes(1));
    }

    private Result handleTagRef(String menuItemName, MenuItemStatus status, Ref tagRef) {
        client.fetch(Tag.class, tagRef.getName()).filter(tag -> tag.getStatus() != null)
            .filter(tag -> StringUtils.hasText(tag.getStatus().getPermalink())).ifPresent(tag -> {
                status.setHref(tag.getStatus().getPermalink());
                status.setDisplayName(tag.getSpec().getDisplayName());
                updateStatus(menuItemName, status);
            });
        return new Result(true, Duration.ofMinutes(1));
    }

    private Result handlePostRef(String menuItemName, MenuItemStatus status, Ref postRef) {
        client.fetch(Post.class, postRef.getName()).filter(post -> post.getStatus() != null)
            .filter(post -> StringUtils.hasText(post.getStatus().getPermalink()))
            .ifPresent(post -> {
                status.setHref(post.getStatus().getPermalink());
                status.setDisplayName(post.getSpec().getTitle());
                updateStatus(menuItemName, status);
            });
        return new Result(true, Duration.ofMinutes(1));
    }

    private Result handleSinglePageSpec(String menuItemName, MenuItemStatus status, Ref pageRef) {
        client.fetch(SinglePage.class, pageRef.getName())
            .filter(page -> page.getStatus() != null)
            .filter(page -> StringUtils.hasText(page.getStatus().getPermalink()))
            .ifPresent(page -> {
                status.setHref(page.getStatus().getPermalink());
                status.setDisplayName(page.getSpec().getTitle());
                updateStatus(menuItemName, status);
            });
        return new Result(true, Duration.ofMinutes(1));
    }

    private Result handleMenuSpec(String menuItemName, MenuItemStatus status, MenuItemSpec spec) {
        if (spec.getHref() != null && StringUtils.hasText(spec.getDisplayName())) {
            status.setHref(spec.getHref());
            status.setDisplayName(spec.getDisplayName());
            updateStatus(menuItemName, status);
        }
        return new Result(false, null);
    }

    private void updateStatus(String menuItemName, MenuItemStatus status) {
        client.fetch(MenuItem.class, menuItemName)
            .filter(menuItem -> !Objects.deepEquals(menuItem.getStatus(), status))
            .ifPresent(menuItem -> {
                menuItem.setStatus(status);
                client.update(menuItem);
            });
    }

}
