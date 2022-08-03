package run.halo.app.core.extension.menu;

import org.springframework.util.StringUtils;
import run.halo.app.core.extension.menu.MenuItem.MenuItemStatus;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.controller.Reconciler;

public class MenuItemReconciler implements Reconciler {

    private final ExtensionClient client;

    public MenuItemReconciler(ExtensionClient client) {
        this.client = client;
    }

    @Override
    public Result reconcile(Request request) {
        client.fetch(MenuItem.class, request.name()).ifPresent(menuItem -> {
            final var spec = menuItem.getSpec();

            if (menuItem.getStatus() == null) {
                menuItem.setStatus(new MenuItemStatus());
            }
            var status = menuItem.getStatus();
            if (spec.getCategoryRef() != null) {
                // TODO resolve permalink from category.
            } else if (spec.getTagRef() != null) {
                // TODO resolve permalink from tag.
            } else if (spec.getPageRef() != null) {
                // TODO resolve permalink from page.
            } else if (spec.getPostRef() != null) {
                // TODO resolve permalink from post.
            } else {
                if (spec.getHref() == null || !StringUtils.hasText(spec.getDisplayName())) {
                    // at last, we resolve permalink from spec.
                    throw new IllegalArgumentException("Invalid permalink or displayName");
                }
                status.setHref(spec.getHref());
                status.setDisplayName(spec.getDisplayName());
                // update status
                client.update(menuItem);
            }
        });
        return new Result(false, null);
    }

}
